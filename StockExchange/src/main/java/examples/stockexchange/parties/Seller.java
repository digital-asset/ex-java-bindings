package examples.stockexchange.parties;

import com.daml.ledger.javaapi.data.CommandsSubmission;
import com.daml.ledger.javaapi.data.DisclosedContract;
import examples.codegen.stockexchange.Offer;
import examples.codegen.stockexchange.Stock;
import examples.stockexchange.Party;
import examples.stockexchange.Utils;
import java.util.UUID;

public class Seller extends Party {
  public Seller(int ledgerApiPort) {
    super(ledgerApiPort, "Seller");
  }

  public void announceStockSaleOffer(
      String quotationProducerPartyId, Stock.ContractId offeredStockContractId) {
    CommandsSubmission commandsSubmission =
        CommandsSubmission.create(
                Utils.APP_ID,
                UUID.randomUUID().toString(),
                new Offer(
                        participantSession.getPartyId(),
                        quotationProducerPartyId,
                        offeredStockContractId)
                    .create()
                    .commands())
            .withWorkflowId("Seller-Offer")
            .withActAs(participantSession.getPartyId());
    participantSession
        .getDamlLedgerClient()
        .getCommandClient()
        .submitAndWait(commandsSubmission)
        .blockingGet();
  }

  public DisclosedContract getStockForDisclosure() {
    return Utils.fetchContractForDisclosure(
        participantSession.getDamlLedgerClient(),
        participantSession.getPartyId(),
        Stock.TEMPLATE_ID);
  }

  public DisclosedContract getOfferForDisclosure() {
    return Utils.fetchContractForDisclosure(
        participantSession.getDamlLedgerClient(),
        participantSession.getPartyId(),
        Offer.TEMPLATE_ID);
  }
}
