// Copyright (c) 2025 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package examples.pingpong.grpc;

import java.util.Optional;
import java.util.UUID;

import com.daml.ledger.api.v2.CommandSubmissionServiceGrpc;
import com.daml.ledger.api.v2.CommandSubmissionServiceGrpc.CommandSubmissionServiceFutureStub;
import com.daml.ledger.api.v2.CommandSubmissionServiceOuterClass.SubmitRequest;
import com.daml.ledger.api.v2.CommandsOuterClass.Command;
import com.daml.ledger.api.v2.CommandsOuterClass.Commands;
import com.daml.ledger.api.v2.CommandsOuterClass.CreateCommand;
import com.daml.ledger.api.v2.ValueOuterClass.Identifier;
import com.daml.ledger.api.v2.ValueOuterClass.Record;
import com.daml.ledger.api.v2.ValueOuterClass.RecordField;
import com.daml.ledger.api.v2.ValueOuterClass.Value;
import com.daml.ledger.api.v2.admin.UserManagementServiceGrpc;
import com.daml.ledger.api.v2.admin.UserManagementServiceGrpc.UserManagementServiceBlockingStub;
import com.daml.ledger.api.v2.admin.UserManagementServiceOuterClass.GetUserRequest;
import com.daml.ledger.api.v2.admin.UserManagementServiceOuterClass.GetUserResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class PingPongGrpcMain {

    // application id used for sending commands
    public static final String APP_ID = "PingPongGrpcApp";

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

        String packageId = Optional.ofNullable(System.getProperty("package.id"))
                .orElseThrow(() -> new RuntimeException("package.id must be specified via sys properties"));

        Identifier pingIdentifier = Identifier.newBuilder()
                .setPackageId(packageId)
                .setModuleName("PingPong")
                .setEntityName("Ping")
                .build();
        Identifier pongIdentifier = Identifier.newBuilder()
                .setPackageId(packageId)
                .setModuleName("PingPong")
                .setEntityName("Pong")
                .build();

        // initialize the ping pong processors for Alice and Bob
        PingPongProcessor aliceProcessor = new PingPongProcessor(aliceParty, channel, pingIdentifier, pongIdentifier);
        PingPongProcessor bobProcessor = new PingPongProcessor(bobParty, channel, pingIdentifier, pongIdentifier);

        // start the processors asynchronously
        aliceProcessor.runIndefinitely();
        bobProcessor.runIndefinitely();

        // send the initial commands for both parties
        createInitialContracts(channel, aliceParty, bobParty, pingIdentifier, numInitialContracts);
        createInitialContracts(channel, bobParty, aliceParty, pingIdentifier, numInitialContracts);


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
     * @param pingIdentifier the PingPong.Ping template identifier
     * @param numContracts   the number of initial contracts to create
     */
    private static void createInitialContracts(ManagedChannel channel, String sender, String receiver, Identifier pingIdentifier, int numContracts) {
        CommandSubmissionServiceFutureStub submissionService = CommandSubmissionServiceGrpc.newFutureStub(channel);

        for (int i = 0; i < numContracts; i++) {
            // command that creates the initial Ping contract with the required parameters according to the model
            Command createCommand = Command.newBuilder().setCreate(
                    CreateCommand.newBuilder()
                            .setTemplateId(pingIdentifier)
                            .setCreateArguments(
                                    Record.newBuilder()
                                            // the identifier for a template's record is the same as the identifier for the template
                                            .setRecordId(pingIdentifier)
                                            .addFields(RecordField.newBuilder().setLabel("sender").setValue(Value.newBuilder().setParty(sender)))
                                            .addFields(RecordField.newBuilder().setLabel("receiver").setValue(Value.newBuilder().setParty(receiver)))
                                            .addFields(RecordField.newBuilder().setLabel("count").setValue(Value.newBuilder().setInt64(0)))
                            )
            ).build();


            SubmitRequest submitRequest = SubmitRequest.newBuilder().setCommands(Commands.newBuilder()
                    .setCommandId(UUID.randomUUID().toString())
                    .setWorkflowId(String.format("Ping-%s-%d", sender, i))
                    .addActAs(sender)
                    .setUserId(APP_ID)
                    .addCommands(createCommand)
            ).build();

            // asynchronously send the commands
            submissionService.submit(submitRequest);
        }
    }

    private static String fetchPartyId(ManagedChannel channel, String userId) {
        UserManagementServiceBlockingStub userManagementService = UserManagementServiceGrpc.newBlockingStub(channel);
        GetUserResponse getUserResponse = userManagementService.getUser(GetUserRequest.newBuilder().setUserId(userId).build());
        return getUserResponse.getUser().getPrimaryParty();
    }
}
