// Copyright (c) 2025 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package examples.pingpong.codegen;

import com.daml.ledger.api.v2.CommandSubmissionServiceGrpc;
import com.daml.ledger.api.v2.CommandSubmissionServiceGrpc.CommandSubmissionServiceFutureStub;
import com.daml.ledger.api.v2.admin.UserManagementServiceGrpc;
import com.daml.ledger.api.v2.admin.UserManagementServiceGrpc.UserManagementServiceBlockingStub;
import com.daml.ledger.api.v2.admin.UserManagementServiceOuterClass.GetUserRequest;
import com.daml.ledger.api.v2.admin.UserManagementServiceOuterClass.GetUserResponse;
import com.daml.ledger.javaapi.data.Command;
import com.daml.ledger.javaapi.data.CommandsSubmission;
import com.daml.ledger.javaapi.data.SubmitRequest;
import examples.pingpong.codegen.pingpong.Ping;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.List;
import java.util.Optional;
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
        int numInitialContracts = args.length == 3 ? Integer.parseInt(args[2]) : 10;

        // Initialize a plaintext gRPC channel
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

        // fetch the party IDs that got created in the Daml init script
        String aliceParty = fetchPartyId(channel, ALICE_USER);
        String bobParty = fetchPartyId(channel, BOB_USER);

        // initialize the ping pong processors for Alice and Bob
        PingPongProcessor aliceProcessor = new PingPongProcessor(aliceParty, channel);
        PingPongProcessor bobProcessor = new PingPongProcessor(bobParty, channel);

        // start the processors asynchronously
        aliceProcessor.runIndefinitely();
        bobProcessor.runIndefinitely();

        // send the initial commands for both parties
        createInitialContracts(channel, aliceParty, bobParty, numInitialContracts);
        createInitialContracts(channel, bobParty, aliceParty, numInitialContracts);


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
     * @param sender         the party that sends the initial Ping contract
     * @param receiver       the party that receives the initial Ping contract
     * @param numContracts   the number of initial contracts to create
     */
    private static void createInitialContracts(ManagedChannel channel, String sender, String receiver, int numContracts) {
        CommandSubmissionServiceFutureStub submissionService = CommandSubmissionServiceGrpc.newFutureStub(channel);

        for (int i = 0; i < numContracts; i++) {
            // command that creates the initial Ping contract with the required parameters according to the model
            List<Command> createCommands = Ping.create(sender, receiver, 0L).commands();

            // wrap the create command in a command submission
            CommandsSubmission commandsSubmission = CommandsSubmission.create(
                    APP_ID,
                    UUID.randomUUID().toString(),
                    Optional.empty(),
                    createCommands)
                    .withActAs(List.of(sender))
                    .withReadAs(List.of(sender))
                    .withWorkflowId(String.format("Ping-%s-%d", sender, i));

            // convert the command submission to a proto data structure
            final var request = SubmitRequest.toProto(commandsSubmission);
            // asynchronously send the request
            submissionService.submit(request);
        }
    }

    private static String fetchPartyId(ManagedChannel channel, String userId) {
        UserManagementServiceBlockingStub userManagementService = UserManagementServiceGrpc.newBlockingStub(channel);
        GetUserResponse getUserResponse = userManagementService.getUser(GetUserRequest.newBuilder().setUserId(userId).build());
        return getUserResponse.getUser().getPrimaryParty();
    }
}
