package examples.stockexchange;

import com.daml.ledger.javaapi.data.*;
import com.daml.ledger.rxjava.DamlLedgerClient;
import java.util.Collections;

public class Utils {
  public static final String APP_ID = "StockExchangeApp";

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
}
