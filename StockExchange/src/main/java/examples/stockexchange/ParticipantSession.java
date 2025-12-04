package examples.stockexchange;

import com.daml.ledger.api.v2.CommandServiceGrpc;
import com.daml.ledger.api.v2.StateServiceGrpc;
import com.daml.ledger.api.v2.admin.UserManagementServiceGrpc;
import com.daml.ledger.api.v2.admin.UserManagementServiceGrpc.UserManagementServiceBlockingStub;
import com.daml.ledger.javaapi.data.GetUserRequest;
import com.daml.ledger.api.v2.StateServiceGrpc.StateServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ParticipantSession implements AutoCloseable {
  private final String partyId;
  private final ManagedChannel channel;
  private final StateServiceBlockingStub stateService;
  private final UserManagementServiceBlockingStub userManagementService;
  private final CommandServiceGrpc.CommandServiceBlockingStub commandService;

  public ParticipantSession(int ledgerApiPort, String userId) {
    channel = ManagedChannelBuilder.forAddress("127.0.0.1", ledgerApiPort).usePlaintext().build();
    stateService = StateServiceGrpc.newBlockingStub(channel);
    userManagementService = UserManagementServiceGrpc.newBlockingStub(channel);
    commandService = CommandServiceGrpc.newBlockingStub(channel);
    this.partyId =
        userManagementService
            .getUser(new GetUserRequest(userId).toProto())
            .getUser()
            .getPrimaryParty();
  }

  public String getPartyId() {
    return partyId;
  }

  public StateServiceBlockingStub getStateService() { return stateService; }
  public CommandServiceGrpc.CommandServiceBlockingStub getCommandService() { return commandService; }

  @Override
  public void close() throws Exception {
    channel.shutdown();
  }
}
