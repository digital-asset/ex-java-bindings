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

public class Offer_Accept extends DamlRecord<Offer_Accept> {
  public static final String _packageId = "33e53a5c42e4c0794de777b3c01b98906e72743b0bba101437bf35e1be50000f";

  public final PriceQuotation.ContractId priceQuotationCid;

  public final String buyer;

  public final IOU.ContractId buyerIou;

  public Offer_Accept(PriceQuotation.ContractId priceQuotationCid, String buyer,
      IOU.ContractId buyerIou) {
    this.priceQuotationCid = priceQuotationCid;
    this.buyer = buyer;
    this.buyerIou = buyerIou;
  }

  /**
   * @deprecated since Daml 2.5.0; use {@code valueDecoder} instead
   */
  @Deprecated
  public static Offer_Accept fromValue(Value value$) throws IllegalArgumentException {
    return valueDecoder().decode(value$);
  }

  public static ValueDecoder<Offer_Accept> valueDecoder() throws IllegalArgumentException {
    return value$ -> {
      Value recordValue$ = value$;
      List<com.daml.ledger.javaapi.data.DamlRecord.Field> fields$ = PrimitiveValueDecoders.recordCheck(3,0,
          recordValue$);
      PriceQuotation.ContractId priceQuotationCid =
          new PriceQuotation.ContractId(fields$.get(0).getValue().asContractId().orElseThrow(() -> new IllegalArgumentException("Expected priceQuotationCid to be of type com.daml.ledger.javaapi.data.ContractId")).getValue());
      String buyer = PrimitiveValueDecoders.fromParty.decode(fields$.get(1).getValue());
      IOU.ContractId buyerIou =
          new IOU.ContractId(fields$.get(2).getValue().asContractId().orElseThrow(() -> new IllegalArgumentException("Expected buyerIou to be of type com.daml.ledger.javaapi.data.ContractId")).getValue());
      return new Offer_Accept(priceQuotationCid, buyer, buyerIou);
    } ;
  }

  public com.daml.ledger.javaapi.data.DamlRecord toValue() {
    ArrayList<com.daml.ledger.javaapi.data.DamlRecord.Field> fields = new ArrayList<com.daml.ledger.javaapi.data.DamlRecord.Field>(3);
    fields.add(new com.daml.ledger.javaapi.data.DamlRecord.Field("priceQuotationCid", this.priceQuotationCid.toValue()));
    fields.add(new com.daml.ledger.javaapi.data.DamlRecord.Field("buyer", new Party(this.buyer)));
    fields.add(new com.daml.ledger.javaapi.data.DamlRecord.Field("buyerIou", this.buyerIou.toValue()));
    return new com.daml.ledger.javaapi.data.DamlRecord(fields);
  }

  public static JsonLfDecoder<Offer_Accept> jsonDecoder() {
    return JsonLfDecoders.record(Arrays.asList("priceQuotationCid", "buyer", "buyerIou"), name -> {
          switch (name) {
            case "priceQuotationCid": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(0, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.contractId(examples.codegen.stockexchange.PriceQuotation.ContractId::new));
            case "buyer": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(1, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.party);
            case "buyerIou": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(2, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.contractId(examples.codegen.stockexchange.IOU.ContractId::new));
            default: return null;
          }
        }
        , (Object[] args) -> new Offer_Accept(JsonLfDecoders.cast(args[0]), JsonLfDecoders.cast(args[1]), JsonLfDecoders.cast(args[2])));
  }

  public static Offer_Accept fromJson(String json) throws JsonLfDecoder.Error {
    return jsonDecoder().decode(new JsonLfReader(json));
  }

  public JsonLfEncoder jsonEncoder() {
    return JsonLfEncoders.record(
        JsonLfEncoders.Field.of("priceQuotationCid", apply(JsonLfEncoders::contractId, priceQuotationCid)),
        JsonLfEncoders.Field.of("buyer", apply(JsonLfEncoders::party, buyer)),
        JsonLfEncoders.Field.of("buyerIou", apply(JsonLfEncoders::contractId, buyerIou)));
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null) {
      return false;
    }
    if (!(object instanceof Offer_Accept)) {
      return false;
    }
    Offer_Accept other = (Offer_Accept) object;
    return Objects.equals(this.priceQuotationCid, other.priceQuotationCid) &&
        Objects.equals(this.buyer, other.buyer) && Objects.equals(this.buyerIou, other.buyerIou);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.priceQuotationCid, this.buyer, this.buyerIou);
  }

  @Override
  public String toString() {
    return String.format("examples.codegen.stockexchange.Offer_Accept(%s, %s, %s)",
        this.priceQuotationCid, this.buyer, this.buyerIou);
  }
}
