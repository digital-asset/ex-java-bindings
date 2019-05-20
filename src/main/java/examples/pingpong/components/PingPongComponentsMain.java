// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package examples.pingpong.components;

import com.daml.ledger.rxjava.DamlLedgerClient;
import com.daml.ledger.rxjava.LedgerClient;
import com.daml.ledger.rxjava.PackageClient;
import com.daml.ledger.rxjava.components.Bot;
import com.daml.ledger.javaapi.data.*;
import com.digitalasset.daml_lf.DamlLf;
import com.digitalasset.daml_lf.DamlLf1;
import com.google.protobuf.InvalidProtocolBufferException;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PingPongComponentsMain {

    private final static Logger logger = LoggerFactory.getLogger(PingPongComponentsMain.class);

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

        // create a client object to access services on the ledger
        DamlLedgerClient client = DamlLedgerClient.forHostWithLedgerIdDiscovery(host, port, Optional.empty());

        // Connects to the ledger and runs initial validation
        client.connect();

        // inspect the packages on the ledger and extract the package id of the package containing the PingPong module
        // this is helpful during development when the package id changes a lot due to frequent changes to the DAML code
        String packageId = detectPingPongPackageId(client);

        String ledgerId = client.getLedgerId();

        logger.info("ledger-id: {}", ledgerId);

        Identifier pingIdentifier = new Identifier(packageId, "PingPong", "Ping");
        Identifier pongIdentifier = new Identifier(packageId, "PingPong", "Pong");

        TransactionFilter pingFilter = filterFor(pingIdentifier, ALICE);
        TransactionFilter pongFilter = filterFor(pongIdentifier, BOB);

        PingPongBot pingBot = new PingPongBot(ledgerId, ALICE, pingIdentifier, "RespondPong");
        PingPongBot pongBot = new PingPongBot(ledgerId, BOB, pongIdentifier, "RespondPing");

        Bot.wire(APP_ID, client, pingFilter, pingBot::process, PingPongBot::getContractInfo);
        Bot.wire(APP_ID, client, pongFilter, pongBot::process, PingPongBot::getContractInfo);

        // send the initial command
        createInitialContracts(client, BOB, ALICE, pingIdentifier, numInitialContracts);

        try {
            // wait a couple of seconds for the processing to finish
            Thread.sleep(5000);
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static TransactionFilter filterFor(Identifier templateId, String party) {
        InclusiveFilter inclusiveFilter = new InclusiveFilter(Collections.singleton(templateId));
        Map<String, Filter> filter = Collections.singletonMap(party, inclusiveFilter);
        return new FiltersByParty(filter);
    }

    /**
     * Creates numContracts number of Ping contracts. The sender is used as the submitting party.
     *
     * @param client         the {@link LedgerClient} object to use for services
     * @param sender         the party that sends the initial Ping contract
     * @param receiver       the party that receives the initial Ping contract
     * @param pingIdentifier the PingPong.Ping template identifier
     * @param numContracts   the number of initial contracts to create
     */
    private static void createInitialContracts(LedgerClient client, String sender, String receiver, Identifier pingIdentifier, int numContracts) {

        for (int i = 0; i < numContracts; i++) {
            // command that creates the initial Ping contract with the required parameters according to the model
            CreateCommand createCommand = new CreateCommand(pingIdentifier,
                    new Record(
                            pingIdentifier,
                            new Record.Field("sender", new Party(sender)),
                            new Record.Field("receiver", new Party(receiver)),
                            new Record.Field("count", new Int64(0))
                    )
            );

            // The wrapper object for commands with more details for the resulting transaction
            client.getCommandSubmissionClient().submit(
                    String.format("Ping-%s-%d", sender, i),
                    APP_ID,
                    UUID.randomUUID().toString(),
                    sender,
                    Instant.EPOCH,
                    Instant.EPOCH.plusSeconds(10),
                    Collections.singletonList(createCommand))
                    .blockingGet();
        }
    }

    /**
     * Inspects all DAML packages that are registered on the ledger and returns the id of the package that contains the PingPong module.
     * This is useful during development when the DAML model changes a lot, so that the package id doesn't need to be updated manually
     * after each change.
     *
     * @param client the initialized client object
     * @return the package id of the example DAML module
     */
    private static String detectPingPongPackageId(LedgerClient client) {
        PackageClient packageService = client.getPackageClient();

        // fetch a list of all package ids available on the ledger
        Flowable<String> packagesIds = packageService.listPackages();

        // fetch all packages and find the package that contains the PingPong module
        String packageId = packagesIds
                .flatMap(p -> packageService.getPackage(p).toFlowable())
                .filter(PingPongComponentsMain::containsPingPongModule)
                .map(GetPackageResponse::getHash)
                .firstElement().blockingGet();

        if (packageId == null) {
            // No package on the ledger contained the PingPong module
            throw new RuntimeException("Module PingPong is not available on the ledger");
        }
        return packageId;
    }

    private static boolean containsPingPongModule(GetPackageResponse getPackageResponse) {
        try {
            // parse the archive payload
            DamlLf.ArchivePayload payload = DamlLf.ArchivePayload.parseFrom(getPackageResponse.getArchivePayload());
            // get the DAML LF package
            DamlLf1.Package lfPackage = payload.getDamlLf1();
            // check if the PingPong module is in the current package package
            Optional<DamlLf1.Module> pingPongModule = lfPackage.getModulesList().stream()
                    .filter(m -> m.getName().getSegmentsList().contains("PingPong")).findFirst();

            if (pingPongModule.isPresent())
                return true;

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return false;
    }
}
