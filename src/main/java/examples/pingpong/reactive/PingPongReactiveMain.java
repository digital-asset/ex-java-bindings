// Copyright (c) 2020, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package examples.pingpong.reactive;

import com.daml.ledger.api.v1.CommandsOuterClass.Command;
import com.daml.ledger.api.v1.CommandsOuterClass.CreateCommand;
import com.daml.ledger.api.v1.ValueOuterClass.Identifier;
import com.daml.ledger.api.v1.ValueOuterClass.RecordField;
import com.daml.ledger.api.v1.ValueOuterClass.Record;
import com.daml.ledger.api.v1.ValueOuterClass.Value;
import com.daml.ledger.javaapi.data.GetPackageResponse;
import com.daml.ledger.rxjava.DamlLedgerClient;
import com.daml.ledger.rxjava.LedgerClient;
import com.daml.ledger.rxjava.PackageClient;
import com.daml.daml_lf_1_14.DamlLf;
import com.daml.daml_lf_1_14.DamlLf1;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ProtocolStringList;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PingPongReactiveMain {

    private final static Logger logger = LoggerFactory.getLogger(PingPongReactiveMain.class);

    // application id used for sending commands
    public static final String APP_ID = "PingPongApp";

    // constants for referring to the parties
    public static final String ALICE = "alice";
    public static final String BOB = "bob";

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

        // create a client object to access services on the ledger
        DamlLedgerClient client = DamlLedgerClient.newBuilder(host, port).build();

        // Connects to the ledger and runs initial validation
        client.connect();

        var users = client.getUserManagementClient().listUsers().blockingGet().getUsers();

        // inspect the packages on the ledger and extract the package id of the package
        // containing the PingPong module
        // this is helpful during development when the package id changes a lot due to
        // frequent changes to the DAML code
        String packageId = Optional.ofNullable(System.getProperty("package.id")).orElseThrow(() -> new RuntimeException("package.id must be specified via sys properties"));
        var pingIdentifier = com.daml.ledger.javaapi.data.Identifier.fromProto(Identifier.newBuilder()
                .setPackageId(packageId).setModuleName("PingPong").setEntityName("Ping").build());
        var pongIdentifier = com.daml.ledger.javaapi.data.Identifier.fromProto(Identifier.newBuilder()
                .setPackageId(packageId).setModuleName("PingPong").setEntityName("Pong").build());
        // initialize the ping pong processors for Alice and Bob
        PingPongProcessor aliceProcessor = new PingPongProcessor(ALICE, client, pingIdentifier, pongIdentifier);
        PingPongProcessor bobProcessor = new PingPongProcessor(BOB, client, pingIdentifier, pongIdentifier);

        // start the processors asynchronously
        aliceProcessor.runIndefinitely();
        bobProcessor.runIndefinitely();

        // send the initial commands for both parties
        createInitialContracts(client, ALICE, BOB, pingIdentifier.toProto(), numInitialContracts);
        createInitialContracts(client, BOB, ALICE, pingIdentifier.toProto(), numInitialContracts);

        try {
            // wait a couple of seconds for the processing to finish
            Thread.sleep(5000);
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates numContracts number of Ping contracts. The sender is used as the
     * submitting party.
     *
     * @param client         the {@link LedgerClient} object to use for services
     * @param sender         the party that sends the initial Ping contract
     * @param receiver       the party that receives the initial Ping contract
     * @param pingIdentifier the PingPong.Ping template identifier
     * @param numContracts   the number of initial contracts to create
     */
    private static void createInitialContracts(LedgerClient client, String sender, String receiver,
            Identifier pingIdentifier, int numContracts) {

        for (int i = 0; i < numContracts; i++) {
            // command that creates the initial Ping contract with the required parameters
            // according to the model

            Command createCommand = Command.newBuilder().setCreate(
                    CreateCommand.newBuilder()
                            .setTemplateId(pingIdentifier)
                            .setCreateArguments(
                                    Record.newBuilder()
                                            // the identifier for a template's record is the same as the identifier for
                                            // the template
                                            .setRecordId(pingIdentifier)
                                            .addFields(RecordField.newBuilder().setLabel("sender")
                                                    .setValue(Value.newBuilder().setParty(sender)))
                                            .addFields(RecordField.newBuilder().setLabel("receiver")
                                                    .setValue(Value.newBuilder().setParty(receiver)))
                                            .addFields(RecordField.newBuilder().setLabel("count")
                                                    .setValue(Value.newBuilder().setInt64(0)))))
                    .build();

            // asynchronously send the commands
            client.getCommandClient().submitAndWait(
                    String.format("Ping-%s-%d", sender, i),
                    APP_ID,
                    UUID.randomUUID().toString(),
                    sender,
                    Collections.singletonList(com.daml.ledger.javaapi.data.Command.fromProtoCommand(createCommand)))
                    .blockingGet();
        }
    }
}
