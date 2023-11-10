package examples.stockexchange;

public abstract class Party implements AutoCloseable {
  protected final ParticipantSession participantSession;

  protected Party(int ledgerApiPort, String userId) {
    this.participantSession = new ParticipantSession(ledgerApiPort, userId);
  }

  public String getPartyId() {
    return participantSession.getPartyId();
  }

  @Override
  public void close() throws Exception {
    participantSession.close();
  }
}
