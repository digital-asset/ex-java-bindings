package examples.stockexchange.parties;

import com.daml.ledger.api.v2.TransactionFilterOuterClass;
import com.daml.ledger.api.v2.ValueOuterClass;
import com.daml.ledger.javaapi.data.*;
import com.daml.ledger.rxjava.StateClient;
import examples.codegen.stockexchange.IOU;
import examples.codegen.stockexchange.Offer;
import examples.codegen.stockexchange.PriceQuotation;
import examples.stockexchange.Common;
import examples.stockexchange.ParticipantSession;
import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Buyer {
  private static final Logger logger = LoggerFactory.getLogger(Buyer.class);

  public static void main(String[] args) throws Exception {
    if (args.length < 2)
      throw new IllegalArgumentException("Arguments: <ledger-api-port> <userId>");
    int ledgerApiPort = Integer.parseInt(args[0]);
    String userId = args[1];

    logger.info("BUYER: Initializing");

    try (ParticipantSession participantSession = new ParticipantSession(ledgerApiPort, userId)) {
      acceptOffer(participantSession);
    }
  }

  private static void acceptOffer(ParticipantSession participantSession) throws IOException {
    logger.info("BUYER: Fetching contract-id of owned IOU");
    StateClient stateClient = participantSession
            .getDamlLedgerClient()
            .getStateClient();

    Long ledgerEnd = stateClient.getLedgerEnd().blockingGet();

    EventFormat eventFormat = IOU.contractFilter().eventFormat(Optional.of(Set.of(participantSession.getPartyId())));

    IOU.ContractId iouCid =
        new IOU.ContractId(
                stateClient
                  .getActiveContracts(eventFormat, ledgerEnd)
                  .blockingFirst()
                  .getContractEntry()
                  .get()
                  .getCreatedEvent()
                  .getContractId());

    logger.info("BUYER: Reading shared disclosed contracts");
    DisclosedContract offer = Common.readDisclosedContract(Common.OFFER_DISCLOSED_CONTRACT_FILE);
    DisclosedContract priceQuotation =
        Common.readDisclosedContract(Common.PRICE_QUOTATION_DISCLOSED_CONTRACT_FILE);
    DisclosedContract stock = Common.readDisclosedContract(Common.STOCK_DISCLOSED_CONTRACT_FILE);

    List<DisclosedContract> disclosedContracts = new java.util.ArrayList<>();
    disclosedContracts.add(priceQuotation);
    disclosedContracts.add(offer);
    disclosedContracts.add(stock);

    List<Command> exerciseAcceptOfferCommand =
        new Offer.ContractId(offer.contractId)
            .exerciseOffer_Accept(
                new PriceQuotation.ContractId(priceQuotation.contractId),
                participantSession.getPartyId(),
                iouCid)
            .commands();

    CommandsSubmission commandsSubmission =
        CommandsSubmission.create(
                Common.APP_ID, UUID.randomUUID().toString(), Optional.empty(), exerciseAcceptOfferCommand)
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
