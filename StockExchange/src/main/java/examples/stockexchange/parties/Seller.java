package examples.stockexchange.parties;

import com.daml.ledger.javaapi.data.*;
import examples.codegen.stockexchange.Offer;
import examples.codegen.stockexchange.Stock;
import examples.stockexchange.Common;
import examples.stockexchange.ParticipantSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.UUID;

public class Seller {
  private static final Logger logger = LoggerFactory.getLogger(Seller.class);
  public static void main(String[] args) throws Exception {
    logger.info("SELLER: Initializing");

    Common.PartyParticipantSetup participantSetup = Common.PartyParticipantSetup.SELLER;
    try (ParticipantSession participantSession =
        new ParticipantSession(participantSetup.getLedgerApiPort(), participantSetup.getUserId())) {
      String stockExchangePartyId =
          Common.readPartyId(Common.PartyParticipantSetup.STOCK_EXCHANGE.getPartyDisplayName());

      logger.info("SELLER: Fetching contract-id of owned Stock");
      Stock.ContractId stockCid =
          new Stock.ContractId(
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
                                      Stock.TEMPLATE_ID,
                                      Filter.Template.HIDE_CREATED_EVENT_BLOB)))),
                      false)
                  .blockingFirst()
                  .getCreatedEvents()
                  .get(0)
                  .getContractId());

      CommandsSubmission commandsSubmission =
          CommandsSubmission.create(
                  Common.APP_ID,
                  UUID.randomUUID().toString(),
                  new Offer(participantSession.getPartyId(), stockExchangePartyId, stockCid)
                      .create()
                      .commands())
              .withWorkflowId("Seller-Offer")
              .withActAs(participantSession.getPartyId());

      logger.info("SELLER: Creating on-ledger Offer for selling owned Stock");
      participantSession
          .getDamlLedgerClient()
          .getCommandClient()
          .submitAndWait(commandsSubmission)
          .blockingGet();

      logger.info("SELLER: Fetching Stock disclosed contract for sharing");
      DisclosedContract stockDisclosedContract = Common.fetchContractForDisclosure(
              participantSession.getDamlLedgerClient(),
              participantSession.getPartyId(),
              Stock.TEMPLATE_ID);

      logger.info("SELLER: Fetching Offer disclosed contract for sharing");
      DisclosedContract offerDisclosedContract = Common.fetchContractForDisclosure(
              participantSession.getDamlLedgerClient(),
              participantSession.getPartyId(),
              Offer.TEMPLATE_ID);

      logger.info("SELLER: Sharing Stock disclosed contract");
      Common.shareDisclosedContract(stockDisclosedContract, Common.STOCK_DISCLOSED_CONTRACT_FILE);

      logger.info("SELLER: Sharing Offer disclosed contract");
      Common.shareDisclosedContract(offerDisclosedContract, Common.OFFER_DISCLOSED_CONTRACT_FILE);

      logger.info("SELLER: Done");
    }
  }
}
