// Copyright (c) 2020, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package examples.bet.reactive;

import com.daml.ledger.javaapi.data.*;
import com.daml.ledger.rxjava.LedgerClient;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.Instant;
import java.time.Duration;

/**
 * This class subscribes to the stream of transactions for a given party and
 * reacts to Ping or Pong contracts.
 */
public class GuestOperatorProcessor {

    private static final Logger logger = LoggerFactory.getLogger(GuestOperatorProcessor.class);

    private final String party;
    private final String ledgerId;
    private LedgerClient client;
    private final long noOfBetRequestsPlaced;
    private long noOfBetsReceived = 0L;
    
    private final Identifier betRequestIdentifier;
    private final Identifier betIdentifier;

    // private long highestBetCountReceivedSoFar = 0L;

    public GuestOperatorProcessor(String party, LedgerClient client, Identifier betRequestIdentifier, Identifier betIdentifier, long numInitialContracts) {
        this.party = party;
        this.ledgerId = client.getLedgerId();
        this.client = client;
        this.betRequestIdentifier = betRequestIdentifier;
        this.betIdentifier = betIdentifier;
        this.noOfBetRequestsPlaced = numInitialContracts;
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
     * Processes a transaction and sends the resulting commands to the Command
     * Submission Service
     *
     * @param tx the Transaction to process
     */
    private void processTransaction(Transaction tx) {
        List<Command> exerciseCommands = tx.getEvents().stream()
                .filter(e -> e instanceof CreatedEvent).map(e -> (CreatedEvent) e)
                .flatMap(e -> processEvent(tx.getWorkflowId(), e))
                .collect(Collectors.toList());

        if (!exerciseCommands.isEmpty()) {
            client.getCommandSubmissionClient().submit(
                    tx.getWorkflowId(),
                    BetReactiveMain.APP_ID,
                    UUID.randomUUID().toString(),
                    party,
                    exerciseCommands);
                    // .blockingGet();
        }
    }

    /**
     * For each {@link CreatedEvent} where the <code>receiver</code> is
     * the current party, exercise the <code>Pong</code> choice of <code>Ping</code>
     * contracts, or the <code>Ping</code>
     * choice of <code>Pong</code> contracts.
     *
     * @param workflowId the workflow the event is part of
     * @param event      the {@link CreatedEvent} to process
     * @return an empty <code>Stream</code> if this event doesn't trigger any action
     *         for this {@link BetProcessor}'s
     *         party
     */
    private Stream<Command> processEvent(String workflowId, CreatedEvent event) {
        Identifier template = event.getTemplateId();
        if (template.equals(betRequestIdentifier)) {
            String contractId = event.getContractId();
            
            // assemble the exercise command
            Command cmd = new ExerciseCommand(
                    template,
                    contractId,
                    "Accept",
                    new DamlRecord(Collections.emptyList()));

            return Stream.of(cmd);
        }
        else if(template.equals(betIdentifier)) {
            // noOfBetsReceived++;

            // // Map<String, Value> fields = event.getArguments().getFieldsMap();
            // // String betCountAsString = fields.get("betCount").asText().get().getValue();
            
            // // if(Long.parseLong(betCountAsString) > highestBetCountReceivedSoFar) {
            // //     highestBetCountReceivedSoFar = Long.parseLong(betCountAsString);
            // // }
            // if(noOfBetsReceived == noOfBetRequestsPlaced) {
            //     Instant finish = Instant.now();
            //     long timeElapsed = Duration.between(BetReactiveMain.start, finish).toMillis();
            //     // System.out.println("highestBetCountReceivedSoFar: " + highestBetCountReceivedSoFar);
            //     System.out.println("ELAPSED TIME TO PROCESS " + noOfBetsReceived + " BETS = " + (timeElapsed/1000.0) + " SECONDS!");
            //     System.out.println("THROUGHPUT = " + (noOfBetsReceived/(timeElapsed/1000.0)) + " BETS/SEC");
            // }
            return Stream.empty();
        }
        else return Stream.empty();
    }
}
