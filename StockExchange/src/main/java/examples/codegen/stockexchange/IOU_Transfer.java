package examples.codegen.stockexchange;

import static com.daml.ledger.javaapi.data.codegen.json.JsonLfEncoders.apply;

import com.daml.ledger.javaapi.data.Int64;
import com.daml.ledger.javaapi.data.Party;
import com.daml.ledger.javaapi.data.Value;
import com.daml.ledger.javaapi.data.codegen.DamlRecord;
import com.daml.ledger.javaapi.data.codegen.PrimitiveValueDecoders;
import com.daml.ledger.javaapi.data.codegen.ValueDecoder;
import com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoder;
import com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders;
import com.daml.ledger.javaapi.data.codegen.json.JsonLfEncoder;
import com.daml.ledger.javaapi.data.codegen.json.JsonLfEncoders;
import com.daml.ledger.javaapi.data.codegen.json.JsonLfReader;
import java.lang.Deprecated;
import java.lang.IllegalArgumentException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class IOU_Transfer extends DamlRecord<IOU_Transfer> {
  public static final String _packageId = "33e53a5c42e4c0794de777b3c01b98906e72743b0bba101437bf35e1be50000f";

  public final String target;

  public final Long amount;

  public IOU_Transfer(String target, Long amount) {
    this.target = target;
    this.amount = amount;
  }

  /**
   * @deprecated since Daml 2.5.0; use {@code valueDecoder} instead
   */
  @Deprecated
  public static IOU_Transfer fromValue(Value value$) throws IllegalArgumentException {
    return valueDecoder().decode(value$);
  }

  public static ValueDecoder<IOU_Transfer> valueDecoder() throws IllegalArgumentException {
    return value$ -> {
      Value recordValue$ = value$;
      List<com.daml.ledger.javaapi.data.DamlRecord.Field> fields$ = PrimitiveValueDecoders.recordCheck(2,0,
          recordValue$);
      String target = PrimitiveValueDecoders.fromParty.decode(fields$.get(0).getValue());
      Long amount = PrimitiveValueDecoders.fromInt64.decode(fields$.get(1).getValue());
      return new IOU_Transfer(target, amount);
    } ;
  }

  public com.daml.ledger.javaapi.data.DamlRecord toValue() {
    ArrayList<com.daml.ledger.javaapi.data.DamlRecord.Field> fields = new ArrayList<com.daml.ledger.javaapi.data.DamlRecord.Field>(2);
    fields.add(new com.daml.ledger.javaapi.data.DamlRecord.Field("target", new Party(this.target)));
    fields.add(new com.daml.ledger.javaapi.data.DamlRecord.Field("amount", new Int64(this.amount)));
    return new com.daml.ledger.javaapi.data.DamlRecord(fields);
  }

  public static JsonLfDecoder<IOU_Transfer> jsonDecoder() {
    return JsonLfDecoders.record(Arrays.asList("target", "amount"), name -> {
          switch (name) {
            case "target": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(0, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.party);
            case "amount": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(1, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.int64);
            default: return null;
          }
        }
        , (Object[] args) -> new IOU_Transfer(JsonLfDecoders.cast(args[0]), JsonLfDecoders.cast(args[1])));
  }

  public static IOU_Transfer fromJson(String json) throws JsonLfDecoder.Error {
    return jsonDecoder().decode(new JsonLfReader(json));
  }

  public JsonLfEncoder jsonEncoder() {
    return JsonLfEncoders.record(
        JsonLfEncoders.Field.of("target", apply(JsonLfEncoders::party, target)),
        JsonLfEncoders.Field.of("amount", apply(JsonLfEncoders::int64, amount)));
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null) {
      return false;
    }
    if (!(object instanceof IOU_Transfer)) {
      return false;
    }
    IOU_Transfer other = (IOU_Transfer) object;
    return Objects.equals(this.target, other.target) && Objects.equals(this.amount, other.amount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.target, this.amount);
  }

  @Override
  public String toString() {
    return String.format("examples.codegen.stockexchange.IOU_Transfer(%s, %s)", this.target,
        this.amount);
  }
}
