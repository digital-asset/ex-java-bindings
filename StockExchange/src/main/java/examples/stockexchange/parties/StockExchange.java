package examples.stockexchange.parties;

import static examples.stockexchange.Utils.APP_ID;
import static examples.stockexchange.Utils.fetchContractForDisclosure;

import com.daml.ledger.javaapi.data.CommandsSubmission;
import com.daml.ledger.javaapi.data.DisclosedContract;
import examples.codegen.stockexchange.PriceQuotation;
import examples.codegen.stockexchange.Stock;
import examples.stockexchange.Party;
import java.util.UUID;

public class StockExchange extends Party {
  public StockExchange(int ledgerApiPort) {
    super(ledgerApiPort, "StockExchange");
  }

  public void issueStock(String stockName, String ownerPartyId) {
    CommandsSubmission commandsSubmission =
        CommandsSubmission.create(
                APP_ID,
                UUID.randomUUID().toString(),
                new Stock(participantSession.getPartyId(), ownerPartyId, stockName)
                    .create()
                    .commands())
            .withWorkflowId("Stock-issue")
            .withActAs(participantSession.getPartyId());
        participantSession
            .getDamlLedgerClient()
            .getCommandClient()
            .submitAndWait(commandsSubmission)
            .blockingGet();
  }

  public void emitPriceQuotation(String stockName, Long value) {
    CommandsSubmission commandsSubmission =
        CommandsSubmission.create(
                APP_ID,
                UUID.randomUUID().toString(),
                new PriceQuotation(participantSession.getPartyId(), stockName, value)
                    .create()
                    .commands())
            .withWorkflowId("PriceQuotation-issue")
            .withActAs(participantSession.getPartyId());

    participantSession
        .getDamlLedgerClient()
        .getCommandClient()
        .submitAndWaitForTransaction(commandsSubmission)
        .blockingGet()
        .getEvents()
        .get(0)
        .getContractId();
  }

  public DisclosedContract getPriceQuotationForDisclosure() {
    return fetchContractForDisclosure(
        participantSession.getDamlLedgerClient(),
        participantSession.getPartyId(),
        PriceQuotation.TEMPLATE_ID);
  }
}
