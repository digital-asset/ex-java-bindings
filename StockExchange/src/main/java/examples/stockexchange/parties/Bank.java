package examples.stockexchange.parties;

import com.daml.ledger.javaapi.data.CommandsSubmission;
import examples.codegen.stockexchange.IOU;
import examples.stockexchange.Common;
import examples.stockexchange.ParticipantSession;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bank {
  private static final Logger logger = LoggerFactory.getLogger(Bank.class);

  public static void main(String[] args) throws Exception {
    if (args.length < 4)
      throw new IllegalArgumentException(
          "Arguments: <ledger-api-port> <userId> <buyerPartyId> <iouNumericValue>");
    int ledgerApiPort = Integer.parseInt(args[0]);
    String userId = args[1];
    String buyerPartyId = args[2];
    long issuedIouValue = Long.parseLong(args[3]);

    logger.info("BANK: Initializing");

    try (ParticipantSession participantSession = new ParticipantSession(ledgerApiPort, userId)) {
      issueIou(participantSession, buyerPartyId, issuedIouValue);
    }
  }

  private static void issueIou(
      ParticipantSession participantSession, String buyerPartyId, long issuedIouValue) {
    CommandsSubmission commandsSubmission =
        CommandsSubmission.create(
                Common.APP_ID,
                UUID.randomUUID().toString(),
                new IOU(participantSession.getPartyId(), buyerPartyId, issuedIouValue)
                    .create()
                    .commands())
            .withWorkflowId("Bank-issue-IOU")
            .withActAs(participantSession.getPartyId());

    logger.info("BANK: Issuing IOU with value {} to {}", issuedIouValue, buyerPartyId);
    participantSession
        .getDamlLedgerClient()
        .getCommandClient()
        .submitAndWait(commandsSubmission)
        .blockingGet();

    logger.info("BANK: Done");
  }
}
