package examples.pingpong.reactive;

import com.daml.ledger.javaapi.data.Identifier;

public class IdentifierCreator {
    private String packageId;
    private static String packageName = "#ex-java-bindings";
    public IdentifierCreator(String packageId) {
        this.packageId = packageId;
    }
    public Identifier pingIdentifier() {
        return Identifier.fromProto(
            com.daml.ledger.api.v1.ValueOuterClass.Identifier.newBuilder()
                .setPackageId(packageName)
                .setModuleName("PingPong")
                .setEntityName("Ping")
                .build()
        );
    }
    public Identifier pinnedPingIdentifier() {
        return Identifier.fromProto(
            com.daml.ledger.api.v1.ValueOuterClass.Identifier.newBuilder()
                .setPackageId(packageId)
                .setModuleName("PingPong")
                .setEntityName("Ping")
                .build()
        );
    }
    public Identifier pongIdentifier() {
        return Identifier.fromProto(
            com.daml.ledger.api.v1.ValueOuterClass.Identifier.newBuilder()
                .setPackageId(packageName)
                .setModuleName("PingPong")
                .setEntityName("Pong")
                .build()
        );
    }
    public Identifier pinnedPongIdentifier() {
        return Identifier.fromProto(
            com.daml.ledger.api.v1.ValueOuterClass.Identifier.newBuilder()
                .setPackageId(packageId)
                .setModuleName("PingPong")
                .setEntityName("Pong")
                .build()
        );
    }
}
