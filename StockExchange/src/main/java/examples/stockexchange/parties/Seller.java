package examples.stockexchange.parties;

import com.daml.ledger.javaapi.data.*;
import examples.codegen.stockexchange.Offer;
import examples.codegen.stockexchange.Stock;
import examples.stockexchange.Party;
import examples.stockexchange.Utils;
import java.util.Collections;
import java.util.UUID;

public class Seller extends Party {
  public Seller(int ledgerApiPort) {
    super(ledgerApiPort, "Seller");
  }

  public void announceStockSaleOffer(String quotationProducerPartyId) {
    // Fetch contract id of owned Stock
    Stock.ContractId stockCid =
        new Stock.ContractId(
            participantSession
                .getDamlLedgerClient()
                .getActiveContractSetClient()
                .getActiveContracts(
                    new FiltersByParty(
                        Collections.singletonMap(
                            getPartyId(),
                            new InclusiveFilter(
                                Collections.emptyMap(),
                                Collections.singletonMap(
                                    Stock.TEMPLATE_ID, Filter.Template.HIDE_CREATED_EVENT_BLOB)))),
                    false)
                .blockingFirst()
                .getCreatedEvents()
                .get(0)
                .getContractId());

    CommandsSubmission commandsSubmission =
        CommandsSubmission.create(
                Utils.APP_ID,
                UUID.randomUUID().toString(),
                new Offer(participantSession.getPartyId(), quotationProducerPartyId, stockCid)
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
