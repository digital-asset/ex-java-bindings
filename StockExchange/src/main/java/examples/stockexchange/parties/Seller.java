package examples.stockexchange.parties;

import com.daml.ledger.javaapi.data.*;
import examples.codegen.stockexchange.Offer;
import examples.codegen.stockexchange.Stock;
import examples.stockexchange.Common;
import examples.stockexchange.ParticipantSession;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Seller {
  private static final Logger logger = LoggerFactory.getLogger(Seller.class);

  public static void main(String[] args) throws Exception {
    logger.info("SELLER: Initializing");

    if (args.length < 3)
      throw new IllegalArgumentException(
          "Arguments: <ledger-api-port> <userId> <stockExchangePartyId>");

    int ledgerApiPort = Integer.parseInt(args[0]);
    String userId = args[1];
    String stockExchangePartyId = args[2];

    try (ParticipantSession participantSession = new ParticipantSession(ledgerApiPort, userId)) {
      announceStockSaleOffer(stockExchangePartyId, participantSession);
    }
  }

  private static void announceStockSaleOffer(
      String stockExchangePartyId, ParticipantSession participantSession) throws IOException {
    logger.info("SELLER: Fetching contract-id of owned Stock");
    FiltersByParty getStockAcsFilter =
        new FiltersByParty(
            Collections.singletonMap(
                participantSession.getPartyId(),
                new InclusiveFilter(
                    Collections.emptyMap(),
                    Collections.singletonMap(
                        Stock.TEMPLATE_ID, Filter.Template.HIDE_CREATED_EVENT_BLOB))));

    Stock.ContractId stockCid =
        new Stock.ContractId(
            participantSession
                .getDamlLedgerClient()
                .getActiveContractSetClient()
                .getActiveContracts(getStockAcsFilter, false)
                .blockingFirst()
                .getCreatedEvents()
                .get(0)
                .getContractId());

    List<Command> createOfferCommand =
        new Offer(participantSession.getPartyId(), stockExchangePartyId, stockCid)
            .create()
            .commands();

    CommandsSubmission commandsSubmission =
        CommandsSubmission.create(Common.APP_ID, UUID.randomUUID().toString(), createOfferCommand)
            .withWorkflowId("Seller-Offer")
            .withActAs(participantSession.getPartyId());

    logger.info("SELLER: Creating on-ledger Offer for selling owned Stock");
    participantSession
        .getDamlLedgerClient()
        .getCommandClient()
        .submitAndWait(commandsSubmission)
        .blockingGet();

    logger.info("SELLER: Fetching Stock disclosed contract for sharing");
    DisclosedContract stockDisclosedContract =
        Common.fetchContractForDisclosure(
            participantSession.getDamlLedgerClient(),
            participantSession.getPartyId(),
            Stock.TEMPLATE_ID);

    logger.info("SELLER: Fetching Offer disclosed contract for sharing");
    DisclosedContract offerDisclosedContract =
        Common.fetchContractForDisclosure(
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
