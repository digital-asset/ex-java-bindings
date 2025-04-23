package examples.stockexchange.parties;

import static examples.stockexchange.Common.APP_ID;
import static examples.stockexchange.Common.fetchContractForDisclosure;

import com.daml.ledger.javaapi.data.Command;
import com.daml.ledger.javaapi.data.CommandsSubmission;
import com.daml.ledger.javaapi.data.DisclosedContract;
import com.daml.ledger.javaapi.data.TransactionFormat;
import examples.codegen.stockexchange.PriceQuotation;
import examples.codegen.stockexchange.Stock;
import examples.stockexchange.Common;
import examples.stockexchange.ParticipantSession;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockExchange {
  private static final Logger logger = LoggerFactory.getLogger(StockExchange.class);

  public static void main(String[] args) throws Exception {
    logger.info("STOCK_EXCHANGE: Initializing");

    if (args.length < 5)
      throw new IllegalArgumentException(
          "Arguments: <ledger-api-port> <userId> <sellerPartyId> <issuedStockName> <issuedStockPriceQuotation>");
    int ledgerApiPort = Integer.parseInt(args[0]);
    String userId = args[1];
    String sellerPartyId = args[2];
    String issuedStockName = args[3];
    long issuedStockPriceQuotation = Long.parseLong(args[4]);

    try (ParticipantSession participantSession = new ParticipantSession(ledgerApiPort, userId)) {
      issueStockAndPriceQuotation(
          sellerPartyId, issuedStockName, issuedStockPriceQuotation, participantSession);
    }
  }

  private static void issueStockAndPriceQuotation(
      String sellerPartyId,
      String issuedStockName,
      long issuedStockPriceQuotation,
      ParticipantSession participantSession)
      throws IOException {
    List<Command> createStockCommand =
        new Stock(participantSession.getPartyId(), sellerPartyId, issuedStockName)
            .create()
            .commands();

    CommandsSubmission issueStockSubmission =
        CommandsSubmission.create(APP_ID, UUID.randomUUID().toString(), Optional.empty(), createStockCommand)
            .withWorkflowId("Stock-issue")
            .withActAs(participantSession.getPartyId());

    logger.info("STOCK_EXCHANGE: Issuing stock with name {} to {}", issuedStockName, sellerPartyId);
    participantSession
        .getDamlLedgerClient()
        .getCommandClient()
        .submitAndWait(issueStockSubmission)
        .blockingGet();

    List<Command> createPriceQuotationCommand =
        new PriceQuotation(
                participantSession.getPartyId(), issuedStockName, issuedStockPriceQuotation)
            .create()
            .commands();

    CommandsSubmission emitPriceQuotationSubmission =
        CommandsSubmission.create(APP_ID, UUID.randomUUID().toString(), Optional.empty(), createPriceQuotationCommand)
            .withWorkflowId("PriceQuotation-issue")
            .withActAs(participantSession.getPartyId());

    TransactionFormat transactionFormat = PriceQuotation.contractFilter().transactionFormat(Optional.of(Set.of(participantSession.getPartyId())));

    logger.info(
        "STOCK_EXCHANGE: Emitting price quotation for {} at value {}",
        issuedStockName,
        issuedStockPriceQuotation);
    participantSession
        .getDamlLedgerClient()
        .getCommandClient()
        .submitAndWaitForTransaction(emitPriceQuotationSubmission, transactionFormat)
        .blockingGet()
        .getEvents()
        .get(0)
        .getContractId();

    logger.info(
        "STOCK_EXCHANGE: Fetching PriceQuotation for stock with name {} for disclosure",
        issuedStockName);
    DisclosedContract priceQuotationDisclosedContract =
        fetchContractForDisclosure(
            participantSession.getDamlLedgerClient(),
            participantSession.getPartyId(),
            PriceQuotation.contractFilter());

    logger.info("STOCK_EXCHANGE: Sharing PriceQuotation disclosed contract");
    Common.shareDisclosedContract(
        priceQuotationDisclosedContract, Common.PRICE_QUOTATION_DISCLOSED_CONTRACT_FILE);
  }
}
