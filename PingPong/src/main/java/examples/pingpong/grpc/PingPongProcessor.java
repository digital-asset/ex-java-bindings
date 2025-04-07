// Copyright (c) 2025 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package examples.pingpong.grpc;

import com.daml.ledger.api.v2.CommandSubmissionServiceGrpc;
import com.daml.ledger.api.v2.CommandSubmissionServiceGrpc.CommandSubmissionServiceBlockingStub;
import com.daml.ledger.api.v2.CommandSubmissionServiceOuterClass.SubmitRequest;
import com.daml.ledger.api.v2.CommandsOuterClass.Command;
import com.daml.ledger.api.v2.CommandsOuterClass.Commands;
import com.daml.ledger.api.v2.CommandsOuterClass.ExerciseCommand;
import com.daml.ledger.api.v2.EventOuterClass.CreatedEvent;
import com.daml.ledger.api.v2.EventOuterClass.Event;
import com.daml.ledger.api.v2.TransactionFilterOuterClass;
import com.daml.ledger.api.v2.TransactionFilterOuterClass.Filters;
import com.daml.ledger.api.v2.TransactionFilterOuterClass.CumulativeFilter;
import com.daml.ledger.api.v2.TransactionFilterOuterClass.TransactionFilter;
import com.daml.ledger.api.v2.TransactionFilterOuterClass.TemplateFilter;
import com.daml.ledger.api.v2.TransactionOuterClass.Transaction;
import com.daml.ledger.api.v2.UpdateServiceGrpc;
import com.daml.ledger.api.v2.UpdateServiceGrpc.UpdateServiceStub;
import com.daml.ledger.api.v2.UpdateServiceOuterClass.GetUpdatesRequest;
import com.daml.ledger.api.v2.UpdateServiceOuterClass.GetUpdatesResponse;
import com.daml.ledger.api.v2.ValueOuterClass.Identifier;
import com.daml.ledger.api.v2.ValueOuterClass.Record;
import com.daml.ledger.api.v2.ValueOuterClass.RecordField;
import com.daml.ledger.api.v2.ValueOuterClass.Value;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class subscribes to the stream of transactions for a given party and reacts to Ping or Pong contracts.
 */
public class PingPongProcessor {

    private final String party;

    private final UpdateServiceStub transactionService;
    private final CommandSubmissionServiceBlockingStub submissionService;

    private final Identifier pingIdentifier;
    private final Identifier pongIdentifier;

    public PingPongProcessor(String party, ManagedChannel channel, Identifier pingIdentifier, Identifier pongIdentifier) {
        this.party = party;
        this.transactionService = UpdateServiceGrpc.newStub(channel);
        this.submissionService = CommandSubmissionServiceGrpc.newBlockingStub(channel);
        this.pingIdentifier = pingIdentifier;
        this.pongIdentifier = pongIdentifier;
    }

    private CumulativeFilter newTemplate(Identifier identifier){
        return CumulativeFilter
                .newBuilder()
                .setTemplateFilter(
                        TemplateFilter
                                .newBuilder()
                                .setTemplateId(identifier)
                                .build()
                )
                .build();
    }

    public void runIndefinitely() {
        // restrict the subscription to ping and pong template types through an inclusive filter
        final var filtersByParty = TransactionFilter.newBuilder()
                .putFiltersByParty(
                        party,
                        Filters
                                .newBuilder()
                                .addCumulative(newTemplate(pingIdentifier))
                                .addCumulative(newTemplate(pongIdentifier))
                                .build(
                        )).build();
        // assemble the request for the transaction stream
        GetUpdatesRequest transactionsRequest = GetUpdatesRequest.newBuilder()
                .setBeginExclusive(0L)
                .setFilter(filtersByParty)
                .setVerbose(true)
                .build();

        // this StreamObserver reacts to transactions and prints a message if an error occurs or the stream gets closed
        StreamObserver<GetUpdatesResponse> transactionObserver = new StreamObserver<GetUpdatesResponse>() {
            @Override
            public void onNext(GetUpdatesResponse value) {
                if(value.hasTransaction()) processTransaction(value.getTransaction());
            }

            @Override
            public void onError(Throwable t) {
                System.err.printf("%s encountered an error while processing transactions!\n", party);
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.printf("%s's transactions stream completed.\n", party);
            }
        };
        System.out.printf("%s starts reading transactions.\n", party);
        transactionService.getUpdates(transactionsRequest, transactionObserver);
    }

    /**
     * Processes a transaction and sends the resulting commands to the Command Submission Service
     *
     * @param tx the Transaction to process
     */
    private void processTransaction(Transaction tx) {
        List<Command> commands = tx.getEventsList().stream()
                .filter(Event::hasCreated).map(Event::getCreated)
                .flatMap(e -> processEvent(tx.getWorkflowId(), e))
                .collect(Collectors.toList());

        if (!commands.isEmpty()) {
            SubmitRequest request = SubmitRequest.newBuilder()
                    .setCommands(Commands.newBuilder()
                            .setCommandId(UUID.randomUUID().toString())
                            .setWorkflowId(tx.getWorkflowId())
                            .addActAs(party)
                            .setUserId(PingPongGrpcMain.APP_ID)
                            .addAllCommands(commands)
                            .build())
                    .build();
            submissionService.submit(request);
        }
    }

    /**
     * For each {@link CreatedEvent} where the <code>receiver</code> is
     * the current party, exercise the <code>Pong</code> choice of <code>Ping</code> contracts, or the <code>Ping</code>
     * choice of <code>Pong</code> contracts.
     *
     * @param workflowId the workflow the event is part of
     * @param event      the {@link CreatedEvent} to process
     * @return an empty <code>Stream</code> if this event doesn't trigger any action for this {@link PingPongProcessor}'s
     * party
     */
    private Stream<Command> processEvent(String workflowId, CreatedEvent event) {
        Identifier template = event.getTemplateId();

        boolean isPingPongModule = template.getModuleName().equals(pingIdentifier.getModuleName());

        boolean isPing = template.getEntityName().equals(pingIdentifier.getEntityName());
        boolean isPong = template.getEntityName().equals(pongIdentifier.getEntityName());

        if (!isPingPongModule || !isPing && !isPong) return Stream.empty();

        Map<String, Value> fields = event
                .getCreateArguments()
                .getFieldsList()
                .stream()
                .collect(Collectors.toMap(RecordField::getLabel, RecordField::getValue));

        // check that this party is set as the receiver of the contract
        boolean thisPartyIsReceiver = fields.get("receiver").getParty().equals(party);

        if (!thisPartyIsReceiver) return Stream.empty();

        String contractId = event.getContractId();
        String choice = isPing ? "RespondPong" : "RespondPing";

        Long count = fields.get("count").getInt64();
        System.out.printf("%s is exercising %s on %s in workflow %s at count %d\n", party, choice, contractId, workflowId, count);

        // assemble the exercise command
        Command cmd = Command
                .newBuilder()
                .setExercise(ExerciseCommand
                        .newBuilder()
                        .setTemplateId(template)
                        .setContractId(contractId)
                        .setChoice(choice)
                        .setChoiceArgument(Value.newBuilder().setRecord(Record.getDefaultInstance())))
                .build();

        return Stream.of(cmd);
    }
}
