package examples.stockexchange.parties;

import com.daml.ledger.javaapi.data.*;
import examples.codegen.stockexchange.IOU;
import examples.codegen.stockexchange.Offer;
import examples.codegen.stockexchange.PriceQuotation;
import examples.stockexchange.Party;
import examples.stockexchange.Utils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Buyer extends Party {
  public Buyer(int ledgerApiPort) {
    super(ledgerApiPort, "Buyer");
  }

  public void buyStock(
      DisclosedContract priceQuotation,
      DisclosedContract offer,
      DisclosedContract stock) {
    // Fetch contractId of owned IOU
    IOU.ContractId iouCid =
        new IOU.ContractId(
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
                                    IOU.TEMPLATE_ID, Filter.Template.HIDE_CREATED_EVENT_BLOB)))),
                    false)
                .blockingFirst()
                .getCreatedEvents()
                .get(0)
                .getContractId());

    List<DisclosedContract> disclosedContracts = new java.util.ArrayList<>();
    disclosedContracts.add(priceQuotation);
    disclosedContracts.add(offer);
    disclosedContracts.add(stock);

    CommandsSubmission commandsSubmission =
        CommandsSubmission.create(
                Utils.APP_ID,
                UUID.randomUUID().toString(),
                new Offer.ContractId(offer.contractId)
                    .exerciseOffer_Accept(
                        new PriceQuotation.ContractId(priceQuotation.contractId),
                        participantSession.getPartyId(),
                        iouCid)
                    .commands())
            .withWorkflowId("Buyer-buy-stock")
            .withDisclosedContracts(disclosedContracts)
            .withActAs(participantSession.getPartyId());

    participantSession
        .getDamlLedgerClient()
        .getCommandClient()
        .submitAndWait(commandsSubmission)
        .blockingGet();
  }
}
