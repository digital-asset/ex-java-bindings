package examples.stockexchange.parties;

import com.daml.ledger.javaapi.data.*;
import examples.codegen.stockexchange.IOU;
import examples.codegen.stockexchange.Offer;
import examples.codegen.stockexchange.PriceQuotation;
import examples.stockexchange.Common;
import examples.stockexchange.ParticipantSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Buyer {
  private static final Logger logger = LoggerFactory.getLogger(Buyer.class);
  public static void main(String[] args) throws Exception {
    logger.info("BUYER: Initializing");
    Common.PartyParticipantSetup participantSetup = Common.PartyParticipantSetup.BUYER;

    try (ParticipantSession participantSession =
        new ParticipantSession(participantSetup.getLedgerApiPort(), participantSetup.getUserId())) {
      logger.info("BUYER: Fetching contract-id of owned IOU");
      IOU.ContractId iouCid =
          new IOU.ContractId(
              participantSession
                  .getDamlLedgerClient()
                  .getActiveContractSetClient()
                  .getActiveContracts(
                      new FiltersByParty(
                          Collections.singletonMap(
                              participantSession.getPartyId(),
                              new InclusiveFilter(
                                  Collections.emptyMap(),
                                  Collections.singletonMap(
                                      IOU.TEMPLATE_ID, Filter.Template.HIDE_CREATED_EVENT_BLOB)))),
                      false)
                  .blockingFirst()
                  .getCreatedEvents()
                  .get(0)
                  .getContractId());

      logger.info("BUYER: Reading shared disclosed contracts");
      DisclosedContract offer = Common.readDisclosedContract(Common.OFFER_DISCLOSED_CONTRACT_FILE);
      DisclosedContract priceQuotation = Common.readDisclosedContract(Common.PRICE_QUOTATION_DISCLOSED_CONTRACT_FILE);
      DisclosedContract stock = Common.readDisclosedContract(Common.STOCK_DISCLOSED_CONTRACT_FILE);

      List<DisclosedContract> disclosedContracts = new java.util.ArrayList<>();
      disclosedContracts.add(priceQuotation);
      disclosedContracts.add(offer);
      disclosedContracts.add(stock);

      CommandsSubmission commandsSubmission =
          CommandsSubmission.create(
                  Common.APP_ID,
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

      logger.info("BUYER: Submitting command for offer acceptance");
      participantSession
          .getDamlLedgerClient()
          .getCommandClient()
          .submitAndWait(commandsSubmission)
          .blockingGet();

      logger.info("BUYER: Success");
    }
  }
}
