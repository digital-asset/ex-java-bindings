package examples.stockexchange;

import com.daml.ledger.javaapi.data.GetUserRequest;
import com.daml.ledger.rxjava.DamlLedgerClient;

public class ParticipantSession implements AutoCloseable {
  private final String partyId;
  private final DamlLedgerClient damlLedgerClient;

  public ParticipantSession(int ledgerApiPort, String userId) {
    damlLedgerClient = DamlLedgerClient.newBuilder("127.0.0.1", ledgerApiPort).build();
    damlLedgerClient.connect();
    this.partyId =
        damlLedgerClient
            .getUserManagementClient()
            .getUser(new GetUserRequest(userId))
            .blockingGet()
            .getUser()
            .getPrimaryParty()
            .orElseThrow(
                () ->
                    new RuntimeException(
                        String.format("Primary party not set for user id %s", userId)));
  }

  public String getPartyId() {
    return partyId;
  }

  public DamlLedgerClient getDamlLedgerClient() {
    return damlLedgerClient;
  }

  @Override
  public void close() throws Exception {
    damlLedgerClient.close();
  }
}
