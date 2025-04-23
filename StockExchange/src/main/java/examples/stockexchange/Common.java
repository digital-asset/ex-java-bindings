package examples.stockexchange;

import com.daml.ledger.javaapi.data.*;
import com.daml.ledger.rxjava.DamlLedgerClient;
import com.google.protobuf.ByteString;
import java.io.*;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class Common {
  public static final String APP_ID = "StockExchangeApp";
  public static final String PRICE_QUOTATION_DISCLOSED_CONTRACT_FILE =
      "temp_stock_exchange_example/price_quotation_disclosed_contract.txt";
  public static final String STOCK_DISCLOSED_CONTRACT_FILE =
      "temp_stock_exchange_example/stock_disclosed_contract.txt";
  public static final String OFFER_DISCLOSED_CONTRACT_FILE =
      "temp_stock_exchange_example/offer_disclosed_contract.txt";

  public static <Ct> DisclosedContract fetchContractForDisclosure(
      DamlLedgerClient client, String reader, ContractFilter<Ct> contractFilter) {
    final var ledgerEnd = client.getStateClient().getLedgerEnd().blockingGet();
    final var eventFormat = contractFilter.withIncludeCreatedEventBlob(true).eventFormat(Optional.of(Set.of(reader)));
    CreatedEvent event =
        client
            .getStateClient()
            .getActiveContracts(eventFormat, ledgerEnd)
            .blockingFirst()
            .getContractEntry()
            .get()
            .getCreatedEvent();
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
}
