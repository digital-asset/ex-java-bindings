package examples.stockexchange;

import com.daml.ledger.javaapi.data.*;
import com.daml.ledger.rxjava.DamlLedgerClient;
import com.google.protobuf.ByteString;
import java.io.*;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

public class Common {
  public static final String APP_ID = "StockExchangeApp";
  public static final String STOCK_NAME = "Daml";
  public static final String PRICE_QUOTATION_DISCLOSED_CONTRACT_FILE =
      "price_quotation_disclosed_contract.txt";
  public static final String STOCK_DISCLOSED_CONTRACT_FILE = "stock_disclosed_contract.txt";
  public static final String OFFER_DISCLOSED_CONTRACT_FILE = "offer_disclosed_contract.txt";
  private static final String PARTIES_FILE_NAME = "stock_exchange_parties.txt";

  public static DisclosedContract fetchContractForDisclosure(
      DamlLedgerClient client, String reader, Identifier templateId) {
    CreatedEvent event =
        client
            .getActiveContractSetClient()
            .getActiveContracts(
                new FiltersByParty(
                    Collections.singletonMap(
                        reader,
                        new InclusiveFilter(
                            Collections.emptyMap(),
                            Collections.singletonMap(
                                templateId, Filter.Template.INCLUDE_CREATED_EVENT_BLOB)))),
                false)
            .blockingFirst()
            .getCreatedEvents()
            .get(0);
    return new DisclosedContract(
        event.getTemplateId(), event.getContractId(), event.getCreatedEventBlob());
  }

  public static void shareDisclosedContract(DisclosedContract disclosedContract, String fileName)
      throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
      writer.append(
          String.format(
              "%s,%s,%s,%s,%s",
              disclosedContract.contractId,
              disclosedContract.templateId.getPackageId(),
              disclosedContract.templateId.getModuleName(),
              disclosedContract.templateId.getEntityName(),
              Base64.getEncoder()
                  .encodeToString(disclosedContract.createdEventBlob.toByteArray())));
    }
  }

  public static DisclosedContract readDisclosedContract(String fileName) throws IOException {
    try (FileReader fr = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(fr)) {
      return Optional.ofNullable(bufferedReader.readLine())
          .map(
              line -> {
                String[] splitted = line.split(",");
                return new DisclosedContract(
                    new Identifier(splitted[1], splitted[2], splitted[3]),
                    splitted[0],
                    ByteString.copyFrom(Base64.getDecoder().decode(splitted[4])));
              })
          .orElseThrow(
              () -> new IllegalArgumentException(String.format("File %s was empty", fileName)));
    }
  }

  public static String readPartyId(String partyDisplayName) throws IOException {
    try (FileReader fr = new FileReader(PARTIES_FILE_NAME);
        BufferedReader br = new BufferedReader(fr)) {
      String line = br.readLine();

      while (line != null) {
        String[] splitted = line.split("=");
        if (splitted[0].equals(partyDisplayName)) return splitted[1];
        line = br.readLine();
      }

      throw new IllegalArgumentException(
          String.format("Not found party with userId %s", partyDisplayName));
    }
  }

  public enum PartyParticipantSetup {
    STOCK_EXCHANGE("StockExchange", "stockExchange", 5011),
    BANK("Bank", "bank", 5021),
    BUYER("Buyer", "buyer", 5031),
    SELLER("Seller", "seller", 5041);

    private final String userId;
    private final int ledgerApiPort;
    private final String partyDisplayName;

    PartyParticipantSetup(String userId, String partyDisplayName, int ledgerApiPort) {
      this.userId = userId;
      this.partyDisplayName = partyDisplayName;
      this.ledgerApiPort = ledgerApiPort;
    }

    public int getLedgerApiPort() {
      return ledgerApiPort;
    }

    public String getUserId() {
      return userId;
    }

    public String getPartyDisplayName() {
      return partyDisplayName;
    }
  }
}
