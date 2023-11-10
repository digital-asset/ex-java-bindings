package examples.stockexchange.parties;

import com.daml.ledger.javaapi.data.CommandsSubmission;
import com.daml.ledger.javaapi.data.DisclosedContract;
import examples.codegen.stockexchange.IOU;
import examples.codegen.stockexchange.Offer;
import examples.codegen.stockexchange.PriceQuotation;
import examples.stockexchange.Party;
import examples.stockexchange.Utils;
import java.util.List;
import java.util.UUID;

public class Buyer extends Party {
  public Buyer(int ledgerApiPort) {
    super(ledgerApiPort, "Buyer");
  }

  public void buyStock(
      DisclosedContract priceQuotation,
      DisclosedContract offer,
      DisclosedContract stock,
      IOU.ContractId iouCid) {
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
