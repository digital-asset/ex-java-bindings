// Copyright (c) 2020, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package examples.bet.reactive;

import com.daml.ledger.javaapi.data.*;
import com.daml.ledger.rxjava.DamlLedgerClient;
import com.daml.ledger.rxjava.LedgerClient;
import com.daml.ledger.rxjava.PackageClient;
import com.digitalasset.daml_lf_1_8.DamlLf;
import com.digitalasset.daml_lf_1_8.DamlLf1;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ProtocolStringList;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.Instant;

public class BetReactiveMain {

    private final static Logger logger = LoggerFactory.getLogger(BetReactiveMain.class);

    // application id used for sending commands
    public static final String APP_ID = "BettingApp";

    // constants for referring to the parties
    public static final String BETTOR_1 = "Bettor1";//::1220d3d69e2c35973f1a5c00b08f7570335fa9829326fe8b14f3fbf6525b896deb22";
    public static final String HOST_OPERATOR = "HKJC";//::1220d3d69e2c35973f1a5c00b08f7570335fa9829326fe8b14f3fbf6525b896deb22";
    public static final String GUEST_OPERATOR = "UKTote";//::1220d3d69e2c35973f1a5c00b08f7570335fa9829326fe8b14f3fbf6525b896deb22";

    public static Instant start;

    public static void main(String[] args) {
        // Extract host and port from arguments
        // if (args.length < 2) {
        // System.err.println("Usage: HOST PORT [NUM_INITIAL_CONTRACTS]");
        // System.exit(-1);
        // }
        String host = "localhost";  //18.162.225.100"; // args[0];
        int port = 7600;    //5011;    //7600; // Integer.parseInt(args[1]);

        int batchSize = 500;

        // each party will create this number of initial Ping contracts
        long numInitialContracts = 20000; // args.length == 3 ? Integer.parseInt(args[2]) : 100;

        // create a client object to access services on the ledger
        DamlLedgerClient client = DamlLedgerClient.newBuilder(host, port).build();

        // Connects to the ledger and runs initial validation
        client.connect();

        // inspect the packages on the ledger and extract the package id of the package
        // containing the PingPong module
        // this is helpful during development when the package id changes a lot due to
        // frequent changes to the DAML code
        String packageId = "ed88dd1bd7598d13247a127fd0198cd15b6968b1b5bbf4f11b411e69394c9003"; // detectBettingPackageId(client);

        Identifier betRequestIdentifier = new Identifier(packageId, "Betting", "BetRequest");
        Identifier betIdentifier = new Identifier(packageId, "Betting", "Bet");

        // initialize the ping pong processors for Alice and Bob
        // BetProcessor bettor1Processor = new BetProcessor(BETTOR_1, client,
        // betRequestIdentifier, betIdentifier);
        BetProcessor guestOperatorProcessor = new BetProcessor(GUEST_OPERATOR, client, betRequestIdentifier,
                betIdentifier, numInitialContracts);

        // start the processors asynchronously
        guestOperatorProcessor.runIndefinitely();

        // // send the initial commands for both parties
        createBetRequests(client, BETTOR_1, HOST_OPERATOR, GUEST_OPERATOR, betRequestIdentifier, numInitialContracts,
                batchSize);

        try {
            // wait a couple of seconds for the processing to finish
            Thread.sleep(500000);
            // System.exit(0);
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
    private static void createBetRequests(LedgerClient client, String bettor, String hostOperator, String guestOperator,
            Identifier betRequestIdentifier, long numContracts, int batchSize) {

        start = Instant.now();

        int batchNo, betNo;
        long betCount = 0L, numOfBatches = numContracts / batchSize;

        // List<Command> createCommandsBatch = new ArrayList<Command>();
        for (batchNo = 1; batchNo <= numOfBatches; batchNo++) {
            List<Command> createCommandsBatch = new ArrayList<Command>();
            for (betNo = 1; betNo <= batchSize; betNo++) {
                betCount++;
                createCommandsBatch.add(new CreateCommand(betRequestIdentifier,
                        new DamlRecord(
                                betRequestIdentifier,
                                new DamlRecord.Field("betId", new Text(batchNo + "_" + betNo)),
                                new DamlRecord.Field("betCount", new Text("" + betCount)),
                                new DamlRecord.Field("raceIds", DamlList.of(new Text("RACE_1"))),
                                new DamlRecord.Field("betType", new Text("PLACE")),
                                new DamlRecord.Field("betStatus", new Text("New")),
                                new DamlRecord.Field("betAmount", new Numeric(new BigDecimal(10.0))),
                                new DamlRecord.Field("payoutAmount", new Numeric(new BigDecimal(0.0))),
                                new DamlRecord.Field("currency", new Text("GBP")),
                                new DamlRecord.Field("betTime", Timestamp.fromInstant(Instant.now())),
                                new DamlRecord.Field("selectionId", new Int64(1)),
                                new DamlRecord.Field("selectionRaceId", new Text("RACE_1")),
                                new DamlRecord.Field("selectionHorseId", new Text("HORSE_1")),
                                new DamlRecord.Field("location", new Text("UK")),
                                new DamlRecord.Field("bettor", new Party(bettor)),
                                new DamlRecord.Field("hostOperator", new Party(hostOperator)),
                                new DamlRecord.Field("guestOperator", new Party(guestOperator)))));
            }

            System.out.println("Submitting batch #" + batchNo + " with " + (betNo - 1) + " bets (last bet in batch: #" + betCount + ")");
            client.getCommandSubmissionClient().submit(
                    "Batch #" + batchNo,
                    APP_ID,
                    UUID.randomUUID().toString(),
                    bettor,
                    createCommandsBatch);

            // createCommandsBatch = new ArrayList<Command>();
        }
    }

    /**
     * Inspects all DAML packages that are registered on the ledger and returns the
     * id of the package that contains the PingPong module.
     * This is useful during development when the DAML model changes a lot, so that
     * the package id doesn't need to be updated manually
     * after each change.
     *
     * @param client the initialized client object
     * @return the package id of the example DAML module
     */
    private static String detectBettingPackageId(LedgerClient client) {
        PackageClient packageService = client.getPackageClient();

        // fetch a list of all package ids available on the ledger
        Flowable<String> packagesIds = packageService.listPackages();

        // fetch all packages and find the package that contains the PingPong module
        String packageId = packagesIds
                .flatMap(p -> packageService.getPackage(p).toFlowable())
                .filter(BetReactiveMain::containsBettingModule)
                .map(GetPackageResponse::getHash)
                .firstElement().blockingGet();

        if (packageId == null) {
            // No package on the ledger contained the PingPong module
            throw new RuntimeException("Module Bet is not available on the ledger");
        }
        return packageId;
    }

    private static boolean containsBettingModule(GetPackageResponse getPackageResponse) {
        try {
            // parse the archive payload
            DamlLf.ArchivePayload payload = DamlLf.ArchivePayload.parseFrom(getPackageResponse.getArchivePayload());
            // get the DAML LF package
            DamlLf1.Package lfPackage = payload.getDamlLf1();
            // extract module names
            List<DamlLf1.InternedDottedName> internedDottedNamesList = lfPackage.getInternedDottedNamesList();
            ProtocolStringList internedStringsList = lfPackage.getInternedStringsList();

            for (DamlLf1.Module module : lfPackage.getModulesList()) {
                DamlLf1.DottedName name = null;
                switch (module.getNameCase()) {
                    case NAME_DNAME:
                        name = module.getNameDname();
                        break;
                    case NAME_INTERNED_DNAME:
                        List<Integer> nameIndexes = internedDottedNamesList.get(module.getNameInternedDname())
                                .getSegmentsInternedStrList();
                        List<String> nameSegments = nameIndexes.stream().map(internedStringsList::get)
                                .collect(Collectors.toList());
                        name = DamlLf1.DottedName.newBuilder().addAllSegments(nameSegments).build();
                        break;
                    case NAME_NOT_SET:
                        break;
                }
                if (name != null && name.getSegmentsList().size() == 1
                        && name.getSegmentsList().get(0).equals("Betting")) {
                    return true;
                }
            }

        } catch (InvalidProtocolBufferException e) {
            logger.error("Error parsing DAML-LF package", e);
            throw new RuntimeException(e);
        }
        return false;
    }
}
