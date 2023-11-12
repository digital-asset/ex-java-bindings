package examples.codegen.stockexchange;

import static com.daml.ledger.javaapi.data.codegen.json.JsonLfEncoders.apply;

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
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Stock_Transfer extends DamlRecord<Stock_Transfer> {
  public static final String _packageId = "33e53a5c42e4c0794de777b3c01b98906e72743b0bba101437bf35e1be50000f";

  public final String newOwner;

  public Stock_Transfer(String newOwner) {
    this.newOwner = newOwner;
  }

  /**
   * @deprecated since Daml 2.5.0; use {@code valueDecoder} instead
   */
  @Deprecated
  public static Stock_Transfer fromValue(Value value$) throws IllegalArgumentException {
    return valueDecoder().decode(value$);
  }

  public static ValueDecoder<Stock_Transfer> valueDecoder() throws IllegalArgumentException {
    return value$ -> {
      Value recordValue$ = value$;
      List<com.daml.ledger.javaapi.data.DamlRecord.Field> fields$ = PrimitiveValueDecoders.recordCheck(1,0,
          recordValue$);
      String newOwner = PrimitiveValueDecoders.fromParty.decode(fields$.get(0).getValue());
      return new Stock_Transfer(newOwner);
    } ;
  }

  public com.daml.ledger.javaapi.data.DamlRecord toValue() {
    ArrayList<com.daml.ledger.javaapi.data.DamlRecord.Field> fields = new ArrayList<com.daml.ledger.javaapi.data.DamlRecord.Field>(1);
    fields.add(new com.daml.ledger.javaapi.data.DamlRecord.Field("newOwner", new Party(this.newOwner)));
    return new com.daml.ledger.javaapi.data.DamlRecord(fields);
  }

  public static JsonLfDecoder<Stock_Transfer> jsonDecoder() {
    return JsonLfDecoders.record(Arrays.asList("newOwner"), name -> {
          switch (name) {
            case "newOwner": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(0, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.party);
            default: return null;
          }
        }
        , (Object[] args) -> new Stock_Transfer(JsonLfDecoders.cast(args[0])));
  }

  public static Stock_Transfer fromJson(String json) throws JsonLfDecoder.Error {
    return jsonDecoder().decode(new JsonLfReader(json));
  }

  public JsonLfEncoder jsonEncoder() {
    return JsonLfEncoders.record(
        JsonLfEncoders.Field.of("newOwner", apply(JsonLfEncoders::party, newOwner)));
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null) {
      return false;
    }
    if (!(object instanceof Stock_Transfer)) {
      return false;
    }
    Stock_Transfer other = (Stock_Transfer) object;
    return Objects.equals(this.newOwner, other.newOwner);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.newOwner);
  }

  @Override
  public String toString() {
    return String.format("examples.codegen.stockexchange.Stock_Transfer(%s)", this.newOwner);
  }
}
