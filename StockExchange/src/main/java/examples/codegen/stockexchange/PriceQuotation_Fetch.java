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

public class PriceQuotation_Fetch extends DamlRecord<PriceQuotation_Fetch> {
  public static final String _packageId = "33e53a5c42e4c0794de777b3c01b98906e72743b0bba101437bf35e1be50000f";

  public final String fetcher;

  public PriceQuotation_Fetch(String fetcher) {
    this.fetcher = fetcher;
  }

  /**
   * @deprecated since Daml 2.5.0; use {@code valueDecoder} instead
   */
  @Deprecated
  public static PriceQuotation_Fetch fromValue(Value value$) throws IllegalArgumentException {
    return valueDecoder().decode(value$);
  }

  public static ValueDecoder<PriceQuotation_Fetch> valueDecoder() throws IllegalArgumentException {
    return value$ -> {
      Value recordValue$ = value$;
      List<com.daml.ledger.javaapi.data.DamlRecord.Field> fields$ = PrimitiveValueDecoders.recordCheck(1,0,
          recordValue$);
      String fetcher = PrimitiveValueDecoders.fromParty.decode(fields$.get(0).getValue());
      return new PriceQuotation_Fetch(fetcher);
    } ;
  }

  public com.daml.ledger.javaapi.data.DamlRecord toValue() {
    ArrayList<com.daml.ledger.javaapi.data.DamlRecord.Field> fields = new ArrayList<com.daml.ledger.javaapi.data.DamlRecord.Field>(1);
    fields.add(new com.daml.ledger.javaapi.data.DamlRecord.Field("fetcher", new Party(this.fetcher)));
    return new com.daml.ledger.javaapi.data.DamlRecord(fields);
  }

  public static JsonLfDecoder<PriceQuotation_Fetch> jsonDecoder() {
    return JsonLfDecoders.record(Arrays.asList("fetcher"), name -> {
          switch (name) {
            case "fetcher": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(0, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.party);
            default: return null;
          }
        }
        , (Object[] args) -> new PriceQuotation_Fetch(JsonLfDecoders.cast(args[0])));
  }

  public static PriceQuotation_Fetch fromJson(String json) throws JsonLfDecoder.Error {
    return jsonDecoder().decode(new JsonLfReader(json));
  }

  public JsonLfEncoder jsonEncoder() {
    return JsonLfEncoders.record(
        JsonLfEncoders.Field.of("fetcher", apply(JsonLfEncoders::party, fetcher)));
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null) {
      return false;
    }
    if (!(object instanceof PriceQuotation_Fetch)) {
      return false;
    }
    PriceQuotation_Fetch other = (PriceQuotation_Fetch) object;
    return Objects.equals(this.fetcher, other.fetcher);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.fetcher);
  }

  @Override
  public String toString() {
    return String.format("examples.codegen.stockexchange.PriceQuotation_Fetch(%s)", this.fetcher);
  }
}
