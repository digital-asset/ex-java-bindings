package examples.stockexchange.parties;

import com.daml.ledger.api.v2.*;
import com.daml.ledger.javaapi.data.*;
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
    StateServiceGrpc.StateServiceBlockingStub stateClient = participantSession.getStateService();

    Long ledgerEnd = stateClient.getLedgerEnd(StateServiceOuterClass.GetLedgerEndRequest.newBuilder().build()).getOffset();

    EventFormat eventFormat = IOU.contractFilter().eventFormat(Optional.of(Set.of(participantSession.getPartyId())));

    IOU.ContractId iouCid =
        new IOU.ContractId(
                stateClient
                  .getActiveContracts(new GetActiveContractsRequest(eventFormat, ledgerEnd).toProto())
                  .next()
                  .getActiveContract()
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
        new Offer.ContractId(validateContractId(offer.contractId, "offer"))
            .exerciseOffer_Accept(
                new PriceQuotation.ContractId(validateContractId(priceQuotation.contractId, "priceQuotation")),
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
        .getCommandService()
        .submitAndWait(SubmitAndWaitRequest.toProto(commandsSubmission));

    logger.info("BUYER: Success");
  }

  private static String validateContractId(Optional<String> contractId, String where) {
    return contractId
      .orElseThrow(
        () -> new RuntimeException(String.format("contractId not set for %s", where)));
  }
}
