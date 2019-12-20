// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package examples.pingpong.grpc;

import com.digitalasset.daml_lf_1_7.DamlLf;
import com.digitalasset.daml_lf_1_7.DamlLf1;
import com.digitalasset.ledger.api.v1.CommandSubmissionServiceGrpc;
import com.digitalasset.ledger.api.v1.CommandSubmissionServiceGrpc.CommandSubmissionServiceFutureStub;
import com.digitalasset.ledger.api.v1.CommandSubmissionServiceOuterClass.SubmitRequest;
import com.digitalasset.ledger.api.v1.CommandsOuterClass.Command;
import com.digitalasset.ledger.api.v1.CommandsOuterClass.Commands;
import com.digitalasset.ledger.api.v1.CommandsOuterClass.CreateCommand;
import com.digitalasset.ledger.api.v1.LedgerIdentityServiceGrpc;
import com.digitalasset.ledger.api.v1.LedgerIdentityServiceGrpc.LedgerIdentityServiceBlockingStub;
import com.digitalasset.ledger.api.v1.LedgerIdentityServiceOuterClass.GetLedgerIdentityRequest;
import com.digitalasset.ledger.api.v1.LedgerIdentityServiceOuterClass.GetLedgerIdentityResponse;
import com.digitalasset.ledger.api.v1.PackageServiceGrpc;
import com.digitalasset.ledger.api.v1.PackageServiceGrpc.PackageServiceBlockingStub;
import com.digitalasset.ledger.api.v1.PackageServiceOuterClass.GetPackageRequest;
import com.digitalasset.ledger.api.v1.PackageServiceOuterClass.GetPackageResponse;
import com.digitalasset.ledger.api.v1.PackageServiceOuterClass.ListPackagesRequest;
import com.digitalasset.ledger.api.v1.PackageServiceOuterClass.ListPackagesResponse;
import com.digitalasset.ledger.api.v1.ValueOuterClass.Identifier;
import com.digitalasset.ledger.api.v1.ValueOuterClass.Record;
import com.digitalasset.ledger.api.v1.ValueOuterClass.RecordField;
import com.digitalasset.ledger.api.v1.ValueOuterClass.Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PingPongGrpcMain {

    // application id used for sending commands
    public static final String APP_ID = "PingPongApp";

    // constants for referring to the parties
    public static final String ALICE = "Alice";
    public static final String BOB = "Bob";

    public static void main(String[] args) {
        // Extract host and port from arguments
        if (args.length < 2) {
            System.err.println("Usage: HOST PORT [NUM_INITIAL_CONTRACTS]");
            System.exit(-1);
        }
        String host = args[0];
        int port = Integer.valueOf(args[1]);

        // each party will create this number of initial Ping contracts
        int numInitialContracts = args.length == 3 ? Integer.valueOf(args[2]) : 10;

        // Initialize a plaintext gRPC channel
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

        // fetch the ledger ID, which is used in subsequent requests sent to the ledger
        String ledgerId = fetchLedgerId(channel);

        // inspect the packages on the ledger and extract the package id of the package containing the PingPong module
        // this is helpful during development when the package id changes a lot due to likely frequent changes to the DAML code
        String packageId = detectPingPongPackageId(channel, ledgerId);

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
        PingPongProcessor aliceProcessor = new PingPongProcessor(ALICE, ledgerId, channel, pingIdentifier, pongIdentifier);
        PingPongProcessor bobProcessor = new PingPongProcessor(BOB, ledgerId, channel, pingIdentifier, pongIdentifier);

        // start the processors asynchronously
        aliceProcessor.runIndefinitely();
        bobProcessor.runIndefinitely();

        // send the initial commands for both parties
        createInitialContracts(channel, ledgerId, ALICE, BOB, pingIdentifier, numInitialContracts);
        createInitialContracts(channel, ledgerId, BOB, ALICE, pingIdentifier, numInitialContracts);


        try {
            // wait a couple of seconds for the processing to finish
            Thread.sleep(5000);
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
     * @param pingIdentifier the PingPong.Ping template identifier
     * @param numContracts   the number of initial contracts to create
     */
    private static void createInitialContracts(ManagedChannel channel, String ledgerId, String sender, String receiver, Identifier pingIdentifier, int numContracts) {
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
                    .setLedgerId(ledgerId)
                    // set current time to ledger effective time (LET)
                    .setLedgerEffectiveTime(Timestamp.newBuilder().setSeconds(Instant.EPOCH.toEpochMilli() / 1000))
                    // set maximum record time (MRT) to now+5s
                    .setMaximumRecordTime(Timestamp.newBuilder().setSeconds(Instant.EPOCH.plusSeconds(5).toEpochMilli() / 1000))
                    .setCommandId(UUID.randomUUID().toString())
                    .setWorkflowId(String.format("Ping-%s-%d", sender, i))
                    .setParty(sender)
                    .setApplicationId(APP_ID)
                    .addCommands(createCommand)
            ).build();

            // asynchronously send the commands
            submissionService.submit(submitRequest);
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


    /**
     * Inspects all DAML packages that are registered on the ledger and returns the id of the package that contains the PingPong module.
     * This is useful during development when the DAML model changes a lot, so that the package id doesn't need to be updated manually
     * after each change.
     *
     * @param channel  the gRPC channel to use for services
     * @param ledgerId the ledger id to use for requests
     * @return the package id of the example DAML module
     */
    private static String detectPingPongPackageId(ManagedChannel channel, String ledgerId) {
        PackageServiceBlockingStub packageService = PackageServiceGrpc.newBlockingStub(channel);

        // fetch a list of all package ids available on the ledger
        ListPackagesResponse packagesResponse = packageService.listPackages(ListPackagesRequest.newBuilder().setLedgerId(ledgerId).build());

        // find the package that contains the PingPong module
        for (String packageId : packagesResponse.getPackageIdsList()) {
            GetPackageResponse getPackageResponse = packageService.getPackage(GetPackageRequest.newBuilder().setLedgerId(ledgerId).setPackageId(packageId).build());
            try {
                // parse the archive payload
                DamlLf.ArchivePayload payload = DamlLf.ArchivePayload.parseFrom(getPackageResponse.getArchivePayload());
                // get the DAML LF package
                DamlLf1.Package lfPackage = payload.getDamlLf1();
                // extract module names
                List<String> internedStrings = lfPackage.getInternedStringsList();
                List<List<String>> internedDName =
                        lfPackage.getInternedDottedNamesList().stream().map(name ->
                                name.getSegmentsInternedStrList().stream().map(internedStrings::get).collect(Collectors.toList())
                        ).collect(Collectors.toList());
                Stream<List<String>> moduleDNames =
                        lfPackage.getModulesList().stream().map(m -> internedDName.get(m.getNameInternedDname()));

                // check if the PingPong module is in the current package
                if (moduleDNames.anyMatch(m -> m.size() == 1 && m.get(0).equals("PingPong")))
                    return packageId;

            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        // No package on the ledger contained the PingPong module
        throw new RuntimeException("Module PingPong is not available on the ledger");
    }

}
