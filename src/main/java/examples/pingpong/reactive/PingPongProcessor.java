// Copyright (c) 2020, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package examples.pingpong.reactive;

import com.daml.ledger.javaapi.data.*;
import com.daml.ledger.rxjava.LedgerClient;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class subscribes to the stream of transactions for a given party and reacts to Ping or Pong contracts.
 */
public class PingPongProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PingPongProcessor.class);

    private final String party;
    private final String ledgerId;
    private LedgerClient client;

    private final Identifier pingIdentifier;
    private final Identifier pongIdentifier;

    public PingPongProcessor(String party, LedgerClient client, Identifier pingIdentifier, Identifier pongIdentifier) {
        this.party = party;
        this.ledgerId = client.getLedgerId();
        this.client = client;
        this.pingIdentifier = pingIdentifier;
        this.pongIdentifier = pongIdentifier;
    }

    public void runIndefinitely() {
        // assemble the request for the transaction stream
        logger.info("{} starts reading transactions.", party);
        Flowable<Transaction> transactions = client.getTransactionsClient().getTransactions(
                LedgerOffset.LedgerEnd.getInstance(),
                new FiltersByParty(Collections.singletonMap(party, NoFilter.instance)), true);
        transactions.forEach(this::processTransaction);
    }

    /**
     * Processes a transaction and sends the resulting commands to the Command Submission Service
     *
     * @param tx the Transaction to process
     */
    private void processTransaction(Transaction tx) {
        List<Command> exerciseCommands = tx.getEvents().stream()
                .filter(e -> e instanceof CreatedEvent).map(e -> (CreatedEvent) e)
                .flatMap(e -> processEvent(tx.getWorkflowId(), e))
                .collect(Collectors.toList());

        if (!exerciseCommands.isEmpty()) {
            client.getCommandClient().submitAndWait(
                    tx.getWorkflowId(),
                    PingPongReactiveMain.APP_ID,
                    UUID.randomUUID().toString(),
                    party,
                    exerciseCommands)
                    .blockingGet();
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

        boolean isPing = template.equals(pingIdentifier);
        boolean isPong = template.equals(pongIdentifier);

        if (!isPing && !isPong) return Stream.empty();

        Map<String, Value> fields = event.getArguments().getFieldsMap();

        // check that this party is set as the receiver of the contract
        boolean thisPartyIsReceiver = fields.get("receiver").asParty().map(receiver -> receiver.getValue().equals(party))
                .orElseThrow(() -> new IllegalStateException("expected 'receiver' to be a party, found " + fields.get("receiver")));

        if (!thisPartyIsReceiver) return Stream.empty();

        String contractId = event.getContractId();
        String choice = isPing ? "RespondPong" : "RespondPing";

        Optional<Long> count = fields.get("count").asInt64().map(Int64::getValue);

        logger.info("{} is exercising {} on {} in workflow {} at count {}", party, choice, contractId, workflowId, count.orElse(-1L));

        // assemble the exercise command
        Command cmd = new ExerciseCommand(
                template,
                contractId,
                choice,
                new Record(Collections.emptyList()));

        return Stream.of(cmd);
    }
}
