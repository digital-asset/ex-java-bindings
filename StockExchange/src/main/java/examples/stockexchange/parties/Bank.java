package examples.stockexchange.parties;

import com.daml.ledger.javaapi.data.CommandsSubmission;
import examples.codegen.stockexchange.IOU;
import examples.stockexchange.Party;
import examples.stockexchange.Utils;
import java.util.UUID;

public class Bank extends Party {
  public Bank(int ledgerApiPort) {
    super(ledgerApiPort, "Bank");
  }

  public void issueIou(String partyId, Long value) {
    CommandsSubmission commandsSubmission =
        CommandsSubmission.create(
                Utils.APP_ID,
                UUID.randomUUID().toString(),
                new IOU(participantSession.getPartyId(), partyId, value).create().commands())
            .withWorkflowId("Bank-issue-IOU")
            .withActAs(participantSession.getPartyId());

        participantSession
            .getDamlLedgerClient()
            .getCommandClient()
            .submitAndWait(commandsSubmission)
            .blockingGet();
  }
}
