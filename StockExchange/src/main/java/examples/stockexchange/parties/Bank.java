package examples.stockexchange.parties;

import com.daml.ledger.javaapi.data.CommandsSubmission;
import examples.codegen.stockexchange.IOU;
import examples.stockexchange.Common;
import examples.stockexchange.ParticipantSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class Bank {
  private static final Logger logger = LoggerFactory.getLogger(Bank.class);
  public static void main(String[] args) throws Exception {
    logger.info("BANK: Initializing");
    Common.PartyParticipantSetup participantSetup = Common.PartyParticipantSetup.BANK;
    try (ParticipantSession participantSession =
        new ParticipantSession(participantSetup.getLedgerApiPort(), participantSetup.getUserId())) {
      String buyerPartyId =
          Common.readPartyId(Common.PartyParticipantSetup.BUYER.getPartyDisplayName());

      long issuedIouValue = 10L;
      CommandsSubmission commandsSubmission =
          CommandsSubmission.create(
                  Common.APP_ID,
                  UUID.randomUUID().toString(),
                  new IOU(participantSession.getPartyId(), buyerPartyId, issuedIouValue).create().commands())
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
}
