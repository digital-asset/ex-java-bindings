// Copyright (c) 2023 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package examples.pingpong.codegen;

import com.daml.ledger.api.v1.CommandSubmissionServiceGrpc;
import com.daml.ledger.api.v1.CommandSubmissionServiceGrpc.CommandSubmissionServiceFutureStub;
import com.daml.ledger.api.v1.LedgerIdentityServiceGrpc;
import com.daml.ledger.api.v1.LedgerIdentityServiceGrpc.LedgerIdentityServiceBlockingStub;
import com.daml.ledger.api.v1.LedgerIdentityServiceOuterClass.GetLedgerIdentityRequest;
import com.daml.ledger.api.v1.LedgerIdentityServiceOuterClass.GetLedgerIdentityResponse;
import com.daml.ledger.api.v1.admin.UserManagementServiceGrpc;
import com.daml.ledger.api.v1.admin.UserManagementServiceGrpc.UserManagementServiceBlockingStub;
import com.daml.ledger.api.v1.admin.UserManagementServiceOuterClass.GetUserRequest;
import com.daml.ledger.api.v1.admin.UserManagementServiceOuterClass.GetUserResponse;
import com.daml.ledger.javaapi.data.Command;
import com.daml.ledger.javaapi.data.CommandsSubmission;
import com.daml.ledger.javaapi.data.SubmitRequest;
import examples.pingpong.codegen.pingpong.Ping;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;


import java.util.List;
import java.util.UUID;

public class PingPongCodegenMain {

    // application id used for sending commands
    public static final String APP_ID = "PingPongCodegenApp";

    // constants for referring to the users with access to the parties
    public static final String ALICE_USER = "alice";
    public static final String BOB_USER = "bob";

    public static void main(String[] args) {
        // Extract host and port from arguments
        if (args.length < 2) {
            System.err.println("Usage: HOST PORT [NUM_INITIAL_CONTRACTS]");
            System.exit(-1);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        // each party will create this number of initial Ping contracts
        int numInitialContracts = args.length == 3 ? Integer.parseInt(args[2]) : 1;

        // Initialize open telemetry
        OpenTelemetryUtil openTelemetry = new OpenTelemetryUtil(APP_ID);

        // Initialize a plaintext gRPC channel
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .intercept(openTelemetry.getClientInterceptor())
                .build();

        // fetch the ledger ID, which is used in subsequent requests sent to the ledger
        String ledgerId = fetchLedgerId(channel);

        // fetch the party IDs that got created in the Daml init script
        String aliceParty = fetchPartyId(channel, ALICE_USER);
        String bobParty = fetchPartyId(channel, BOB_USER);

        // initialize the ping pong processors for Alice and Bob
        PingPongProcessor aliceProcessor = new PingPongProcessor(aliceParty, ledgerId, channel, openTelemetry);
        PingPongProcessor bobProcessor = new PingPongProcessor(bobParty, ledgerId, channel, openTelemetry);

        // start the processors asynchronously
        aliceProcessor.runIndefinitely();
        bobProcessor.runIndefinitely();

        // send the initial commands for both parties
        createInitialContracts(channel, ledgerId, aliceParty, bobParty, numInitialContracts, openTelemetry.getTracer());
        createInitialContracts(channel, ledgerId, bobParty, aliceParty, numInitialContracts, openTelemetry.getTracer());


        try {
            // wait a couple of seconds for the processing to finish
            Thread.sleep(15000);
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates numContracts number of Ping contracts. The sender is used as the submitting party.
     *
     * @param channel        the gRPC channel to use for services
     * @param ledgerId       the previously fetched ledger id
     * @param sender         the party that sends the initial Ping contract
     * @param receiver       the party that receives the initial Ping contract
     * @param numContracts   the number of initial contracts to create
     */
    private static void createInitialContracts(ManagedChannel channel, String ledgerId, String sender, String receiver, int numContracts, Tracer tracer) {
        CommandSubmissionServiceFutureStub submissionService = CommandSubmissionServiceGrpc.newFutureStub(channel);

        for (int i = 0; i < numContracts; i++) {
            // command that creates the initial Ping contract with the required parameters according to the model
            List<Command> createCommands = Ping.create(sender, receiver, 0L).commands();

            // wrap the create command in a command submission
            CommandsSubmission commandsSubmission = CommandsSubmission.create(
                    APP_ID,
                    UUID.randomUUID().toString(),
                    createCommands)
                    .withActAs(List.of(sender))
                    .withReadAs(List.of(sender))
                    .withWorkflowId(String.format("Ping-%s-%d", sender, i));

            // convert the command submission to a proto data structure
            final var request = SubmitRequest.toProto(ledgerId, commandsSubmission);
            // asynchronously send the request
            Span span = tracer.spanBuilder("createInitialContracts").startSpan();
            try(Scope ignored = span.makeCurrent()) {
                submissionService.submit(request);
            } finally {
                span.end();
            }
        }
    }

    /**
     * Fetches the ledger id via the Ledger Identity Service.
     *
     * @param channel the gRPC channel to use for services
     * @return the ledger id as provided by the ledger
     */
    private static String fetchLedgerId(ManagedChannel channel) {
        LedgerIdentityServiceBlockingStub ledgerIdService = LedgerIdentityServiceGrpc.newBlockingStub(channel);
        GetLedgerIdentityResponse identityResponse = ledgerIdService.getLedgerIdentity(GetLedgerIdentityRequest.getDefaultInstance());
        return identityResponse.getLedgerId();
    }

    private static String fetchPartyId(ManagedChannel channel, String userId) {
        UserManagementServiceBlockingStub userManagementService = UserManagementServiceGrpc.newBlockingStub(channel);
        GetUserResponse getUserResponse = userManagementService.getUser(GetUserRequest.newBuilder().setUserId(userId).build());
        return getUserResponse.getUser().getPrimaryParty();
    }


}
