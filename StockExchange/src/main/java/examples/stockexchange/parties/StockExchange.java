package examples.stockexchange.parties;

import static examples.stockexchange.Common.APP_ID;
import static examples.stockexchange.Common.fetchContractForDisclosure;

import com.daml.ledger.javaapi.data.CommandsSubmission;
import com.daml.ledger.javaapi.data.DisclosedContract;
import examples.codegen.stockexchange.PriceQuotation;
import examples.codegen.stockexchange.Stock;
import examples.stockexchange.Common;
import examples.stockexchange.ParticipantSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class StockExchange {
  private static final Logger logger = LoggerFactory.getLogger(StockExchange.class);

  public static void main(String[] args) throws Exception {
    logger.info("STOCK_EXCHANGE: Initializing");
    Common.PartyParticipantSetup participantSetup = Common.PartyParticipantSetup.STOCK_EXCHANGE;
    try (ParticipantSession participantSession =
        new ParticipantSession(participantSetup.getLedgerApiPort(), participantSetup.getUserId())) {
      String sellerPartyId =
          Common.readPartyId(Common.PartyParticipantSetup.SELLER.getPartyDisplayName());

      // Issue stock
      CommandsSubmission issueStockSubmission =
          CommandsSubmission.create(
                  APP_ID,
                  UUID.randomUUID().toString(),
                  new Stock(participantSession.getPartyId(), sellerPartyId, Common.STOCK_NAME)
                      .create()
                      .commands())
              .withWorkflowId("Stock-issue")
              .withActAs(participantSession.getPartyId());

      logger.info("STOCK_EXCHANGE: Issuing stock with name {} to {}", Common.STOCK_NAME, sellerPartyId);
      participantSession
          .getDamlLedgerClient()
          .getCommandClient()
          .submitAndWait(issueStockSubmission)
          .blockingGet();

      long priceQuotation = 3L;
      CommandsSubmission emitPriceQuotationSubmission =
          CommandsSubmission.create(
                  APP_ID,
                  UUID.randomUUID().toString(),
                  new PriceQuotation(participantSession.getPartyId(), Common.STOCK_NAME, priceQuotation)
                      .create()
                      .commands())
              .withWorkflowId("PriceQuotation-issue")
              .withActAs(participantSession.getPartyId());

      logger.info("STOCK_EXCHANGE: Emitting price quotation for {} at value {}", Common.STOCK_NAME, priceQuotation);
      participantSession
          .getDamlLedgerClient()
          .getCommandClient()
          .submitAndWaitForTransaction(emitPriceQuotationSubmission)
          .blockingGet()
          .getEvents()
          .get(0)
          .getContractId();

    logger.info("STOCK_EXCHANGE: Fetching PriceQuotation for stock with name {} for disclosure", Common.STOCK_NAME);
      DisclosedContract priceQuotationDisclosedContract =
          fetchContractForDisclosure(
              participantSession.getDamlLedgerClient(),
              participantSession.getPartyId(),
              PriceQuotation.TEMPLATE_ID);

      logger.info("STOCK_EXCHANGE: Sharing PriceQuotation disclosed contract");
      Common.shareDisclosedContract(
          priceQuotationDisclosedContract, Common.PRICE_QUOTATION_DISCLOSED_CONTRACT_FILE);
    }
  }
}
