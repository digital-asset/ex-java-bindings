package examples.codegen.stockexchange;

import static com.daml.ledger.javaapi.data.codegen.json.JsonLfEncoders.apply;

import com.daml.ledger.javaapi.data.ContractFilter;
import com.daml.ledger.javaapi.data.CreateAndExerciseCommand;
import com.daml.ledger.javaapi.data.CreateCommand;
import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.DamlRecord;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Party;
import com.daml.ledger.javaapi.data.Template;
import com.daml.ledger.javaapi.data.Unit;
import com.daml.ledger.javaapi.data.Value;
import com.daml.ledger.javaapi.data.codegen.Choice;
import com.daml.ledger.javaapi.data.codegen.ContractCompanion;
import com.daml.ledger.javaapi.data.codegen.ContractTypeCompanion;
import com.daml.ledger.javaapi.data.codegen.Created;
import com.daml.ledger.javaapi.data.codegen.Exercised;
import com.daml.ledger.javaapi.data.codegen.PrimitiveValueDecoders;
import com.daml.ledger.javaapi.data.codegen.Update;
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
import java.util.Optional;
import java.util.Set;

public final class Offer extends Template {
  public static final Identifier TEMPLATE_ID = new Identifier("33e53a5c42e4c0794de777b3c01b98906e72743b0bba101437bf35e1be50000f", "StockExchange", "Offer");

  public static final Choice<Offer, examples.codegen.da.internal.template.Archive, Unit> CHOICE_Archive = 
      Choice.create("Archive", value$ -> value$.toValue(), value$ ->
        examples.codegen.da.internal.template.Archive.valueDecoder().decode(value$), value$ ->
        PrimitiveValueDecoders.fromUnit.decode(value$));

  public static final Choice<Offer, Offer_Accept, Unit> CHOICE_Offer_Accept = 
      Choice.create("Offer_Accept", value$ -> value$.toValue(), value$ ->
        Offer_Accept.valueDecoder().decode(value$), value$ -> PrimitiveValueDecoders.fromUnit
        .decode(value$));

  public static final ContractCompanion.WithoutKey<Contract, ContractId, Offer> COMPANION = 
      new ContractCompanion.WithoutKey<>("examples.codegen.stockexchange.Offer", TEMPLATE_ID,
        ContractId::new, v -> Offer.templateValueDecoder().decode(v), Offer::fromJson,
        Contract::new, List.of(CHOICE_Archive, CHOICE_Offer_Accept));

  public final String seller;

  public final String quotationProducer;

  public final Stock.ContractId offeredAssetCid;

  public Offer(String seller, String quotationProducer, Stock.ContractId offeredAssetCid) {
    this.seller = seller;
    this.quotationProducer = quotationProducer;
    this.offeredAssetCid = offeredAssetCid;
  }

  @Override
  public Update<Created<ContractId>> create() {
    return new Update.CreateUpdate<ContractId, Created<ContractId>>(new CreateCommand(Offer.TEMPLATE_ID, this.toValue()), x -> x, ContractId::new);
  }

  /**
   * @deprecated since Daml 2.3.0; use {@code createAnd().exerciseArchive} instead
   */
  @Deprecated
  public Update<Exercised<Unit>> createAndExerciseArchive(
      examples.codegen.da.internal.template.Archive arg) {
    return createAnd().exerciseArchive(arg);
  }

  /**
   * @deprecated since Daml 2.3.0; use {@code createAnd().exerciseArchive} instead
   */
  @Deprecated
  public Update<Exercised<Unit>> createAndExerciseArchive() {
    return createAndExerciseArchive(new examples.codegen.da.internal.template.Archive());
  }

  /**
   * @deprecated since Daml 2.3.0; use {@code createAnd().exerciseOffer_Accept} instead
   */
  @Deprecated
  public Update<Exercised<Unit>> createAndExerciseOffer_Accept(Offer_Accept arg) {
    return createAnd().exerciseOffer_Accept(arg);
  }

  /**
   * @deprecated since Daml 2.3.0; use {@code createAnd().exerciseOffer_Accept} instead
   */
  @Deprecated
  public Update<Exercised<Unit>> createAndExerciseOffer_Accept(
      PriceQuotation.ContractId priceQuotationCid, String buyer, IOU.ContractId buyerIou) {
    return createAndExerciseOffer_Accept(new Offer_Accept(priceQuotationCid, buyer, buyerIou));
  }

  public static Update<Created<ContractId>> create(String seller, String quotationProducer,
      Stock.ContractId offeredAssetCid) {
    return new Offer(seller, quotationProducer, offeredAssetCid).create();
  }

  @Override
  public CreateAnd createAnd() {
    return new CreateAnd(this);
  }

  @Override
  protected ContractCompanion.WithoutKey<Contract, ContractId, Offer> getCompanion() {
    return COMPANION;
  }

  /**
   * @deprecated since Daml 2.5.0; use {@code valueDecoder} instead
   */
  @Deprecated
  public static Offer fromValue(Value value$) throws IllegalArgumentException {
    return valueDecoder().decode(value$);
  }

  public static ValueDecoder<Offer> valueDecoder() throws IllegalArgumentException {
    return ContractCompanion.valueDecoder(COMPANION);
  }

  public DamlRecord toValue() {
    ArrayList<DamlRecord.Field> fields = new ArrayList<DamlRecord.Field>(3);
    fields.add(new DamlRecord.Field("seller", new Party(this.seller)));
    fields.add(new DamlRecord.Field("quotationProducer", new Party(this.quotationProducer)));
    fields.add(new DamlRecord.Field("offeredAssetCid", this.offeredAssetCid.toValue()));
    return new DamlRecord(fields);
  }

  private static ValueDecoder<Offer> templateValueDecoder() throws IllegalArgumentException {
    return value$ -> {
      Value recordValue$ = value$;
      List<DamlRecord.Field> fields$ = PrimitiveValueDecoders.recordCheck(3,0, recordValue$);
      String seller = PrimitiveValueDecoders.fromParty.decode(fields$.get(0).getValue());
      String quotationProducer = PrimitiveValueDecoders.fromParty.decode(fields$.get(1).getValue());
      Stock.ContractId offeredAssetCid =
          new Stock.ContractId(fields$.get(2).getValue().asContractId().orElseThrow(() -> new IllegalArgumentException("Expected offeredAssetCid to be of type com.daml.ledger.javaapi.data.ContractId")).getValue());
      return new Offer(seller, quotationProducer, offeredAssetCid);
    } ;
  }

  public static JsonLfDecoder<Offer> jsonDecoder() {
    return JsonLfDecoders.record(Arrays.asList("seller", "quotationProducer", "offeredAssetCid"), name -> {
          switch (name) {
            case "seller": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(0, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.party);
            case "quotationProducer": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(1, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.party);
            case "offeredAssetCid": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(2, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.contractId(examples.codegen.stockexchange.Stock.ContractId::new));
            default: return null;
          }
        }
        , (Object[] args) -> new Offer(JsonLfDecoders.cast(args[0]), JsonLfDecoders.cast(args[1]), JsonLfDecoders.cast(args[2])));
  }

  public static Offer fromJson(String json) throws JsonLfDecoder.Error {
    return jsonDecoder().decode(new JsonLfReader(json));
  }

  public JsonLfEncoder jsonEncoder() {
    return JsonLfEncoders.record(
        JsonLfEncoders.Field.of("seller", apply(JsonLfEncoders::party, seller)),
        JsonLfEncoders.Field.of("quotationProducer", apply(JsonLfEncoders::party, quotationProducer)),
        JsonLfEncoders.Field.of("offeredAssetCid", apply(JsonLfEncoders::contractId, offeredAssetCid)));
  }

  public static ContractFilter<Contract> contractFilter() {
    return ContractFilter.of(COMPANION);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null) {
      return false;
    }
    if (!(object instanceof Offer)) {
      return false;
    }
    Offer other = (Offer) object;
    return Objects.equals(this.seller, other.seller) &&
        Objects.equals(this.quotationProducer, other.quotationProducer) &&
        Objects.equals(this.offeredAssetCid, other.offeredAssetCid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.seller, this.quotationProducer, this.offeredAssetCid);
  }

  @Override
  public String toString() {
    return String.format("examples.codegen.stockexchange.Offer(%s, %s, %s)", this.seller,
        this.quotationProducer, this.offeredAssetCid);
  }

  public static final class ContractId extends com.daml.ledger.javaapi.data.codegen.ContractId<Offer> implements Exercises<ExerciseCommand> {
    public ContractId(String contractId) {
      super(contractId);
    }

    @Override
    protected ContractTypeCompanion<? extends com.daml.ledger.javaapi.data.codegen.Contract<ContractId, ?>, ContractId, Offer, ?> getCompanion(
        ) {
      return COMPANION;
    }

    public static ContractId fromContractId(
        com.daml.ledger.javaapi.data.codegen.ContractId<Offer> contractId) {
      return COMPANION.toContractId(contractId);
    }
  }

  public static class Contract extends com.daml.ledger.javaapi.data.codegen.Contract<ContractId, Offer> {
    public Contract(ContractId id, Offer data, Optional<String> agreementText,
        Set<String> signatories, Set<String> observers) {
      super(id, data, agreementText, signatories, observers);
    }

    @Override
    protected ContractCompanion<Contract, ContractId, Offer> getCompanion() {
      return COMPANION;
    }

    public static Contract fromIdAndRecord(String contractId, DamlRecord record$,
        Optional<String> agreementText, Set<String> signatories, Set<String> observers) {
      return COMPANION.fromIdAndRecord(contractId, record$, agreementText, signatories, observers);
    }

    public static Contract fromCreatedEvent(CreatedEvent event) {
      return COMPANION.fromCreatedEvent(event);
    }
  }

  public interface Exercises<Cmd> extends com.daml.ledger.javaapi.data.codegen.Exercises.Archive<Cmd> {
    default Update<Exercised<Unit>> exerciseArchive(
        examples.codegen.da.internal.template.Archive arg) {
      return makeExerciseCmd(CHOICE_Archive, arg);
    }

    default Update<Exercised<Unit>> exerciseArchive() {
      return exerciseArchive(new examples.codegen.da.internal.template.Archive());
    }

    default Update<Exercised<Unit>> exerciseOffer_Accept(Offer_Accept arg) {
      return makeExerciseCmd(CHOICE_Offer_Accept, arg);
    }

    default Update<Exercised<Unit>> exerciseOffer_Accept(
        PriceQuotation.ContractId priceQuotationCid, String buyer, IOU.ContractId buyerIou) {
      return exerciseOffer_Accept(new Offer_Accept(priceQuotationCid, buyer, buyerIou));
    }
  }

  public static final class CreateAnd extends com.daml.ledger.javaapi.data.codegen.CreateAnd implements Exercises<CreateAndExerciseCommand> {
    CreateAnd(Template createArguments) {
      super(createArguments);
    }

    @Override
    protected ContractTypeCompanion<? extends com.daml.ledger.javaapi.data.codegen.Contract<ContractId, ?>, ContractId, Offer, ?> getCompanion(
        ) {
      return COMPANION;
    }
  }
}
