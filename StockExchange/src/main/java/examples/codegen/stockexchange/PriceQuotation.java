package examples.codegen.stockexchange;

import static com.daml.ledger.javaapi.data.codegen.json.JsonLfEncoders.apply;

import com.daml.ledger.javaapi.data.ContractFilter;
import com.daml.ledger.javaapi.data.CreateAndExerciseCommand;
import com.daml.ledger.javaapi.data.CreateCommand;
import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.DamlRecord;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Int64;
import com.daml.ledger.javaapi.data.Party;
import com.daml.ledger.javaapi.data.Template;
import com.daml.ledger.javaapi.data.Text;
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
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class PriceQuotation extends Template {
  public static final Identifier TEMPLATE_ID = new Identifier("33e53a5c42e4c0794de777b3c01b98906e72743b0bba101437bf35e1be50000f", "StockExchange", "PriceQuotation");

  public static final Choice<PriceQuotation, PriceQuotation_Fetch, PriceQuotation> CHOICE_PriceQuotation_Fetch = 
      Choice.create("PriceQuotation_Fetch", value$ -> value$.toValue(), value$ ->
        PriceQuotation_Fetch.valueDecoder().decode(value$), value$ -> PriceQuotation.valueDecoder()
        .decode(value$));

  public static final Choice<PriceQuotation, examples.codegen.da.internal.template.Archive, Unit> CHOICE_Archive = 
      Choice.create("Archive", value$ -> value$.toValue(), value$ ->
        examples.codegen.da.internal.template.Archive.valueDecoder().decode(value$), value$ ->
        PrimitiveValueDecoders.fromUnit.decode(value$));

  public static final ContractCompanion.WithoutKey<Contract, ContractId, PriceQuotation> COMPANION = 
      new ContractCompanion.WithoutKey<>("examples.codegen.stockexchange.PriceQuotation",
        TEMPLATE_ID, ContractId::new, v -> PriceQuotation.templateValueDecoder().decode(v),
        PriceQuotation::fromJson, Contract::new, List.of(CHOICE_PriceQuotation_Fetch,
        CHOICE_Archive));

  public final String issuer;

  public final String stockName;

  public final Long value;

  public PriceQuotation(String issuer, String stockName, Long value) {
    this.issuer = issuer;
    this.stockName = stockName;
    this.value = value;
  }

  @Override
  public Update<Created<ContractId>> create() {
    return new Update.CreateUpdate<ContractId, Created<ContractId>>(new CreateCommand(PriceQuotation.TEMPLATE_ID, this.toValue()), x -> x, ContractId::new);
  }

  /**
   * @deprecated since Daml 2.3.0; use {@code createAnd().exercisePriceQuotation_Fetch} instead
   */
  @Deprecated
  public Update<Exercised<PriceQuotation>> createAndExercisePriceQuotation_Fetch(
      PriceQuotation_Fetch arg) {
    return createAnd().exercisePriceQuotation_Fetch(arg);
  }

  /**
   * @deprecated since Daml 2.3.0; use {@code createAnd().exercisePriceQuotation_Fetch} instead
   */
  @Deprecated
  public Update<Exercised<PriceQuotation>> createAndExercisePriceQuotation_Fetch(String fetcher) {
    return createAndExercisePriceQuotation_Fetch(new PriceQuotation_Fetch(fetcher));
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

  public static Update<Created<ContractId>> create(String issuer, String stockName, Long value) {
    return new PriceQuotation(issuer, stockName, value).create();
  }

  @Override
  public CreateAnd createAnd() {
    return new CreateAnd(this);
  }

  @Override
  protected ContractCompanion.WithoutKey<Contract, ContractId, PriceQuotation> getCompanion() {
    return COMPANION;
  }

  /**
   * @deprecated since Daml 2.5.0; use {@code valueDecoder} instead
   */
  @Deprecated
  public static PriceQuotation fromValue(Value value$) throws IllegalArgumentException {
    return valueDecoder().decode(value$);
  }

  public static ValueDecoder<PriceQuotation> valueDecoder() throws IllegalArgumentException {
    return ContractCompanion.valueDecoder(COMPANION);
  }

  public DamlRecord toValue() {
    ArrayList<DamlRecord.Field> fields = new ArrayList<DamlRecord.Field>(3);
    fields.add(new DamlRecord.Field("issuer", new Party(this.issuer)));
    fields.add(new DamlRecord.Field("stockName", new Text(this.stockName)));
    fields.add(new DamlRecord.Field("value", new Int64(this.value)));
    return new DamlRecord(fields);
  }

  private static ValueDecoder<PriceQuotation> templateValueDecoder() throws
      IllegalArgumentException {
    return value$ -> {
      Value recordValue$ = value$;
      List<DamlRecord.Field> fields$ = PrimitiveValueDecoders.recordCheck(3,0, recordValue$);
      String issuer = PrimitiveValueDecoders.fromParty.decode(fields$.get(0).getValue());
      String stockName = PrimitiveValueDecoders.fromText.decode(fields$.get(1).getValue());
      Long value = PrimitiveValueDecoders.fromInt64.decode(fields$.get(2).getValue());
      return new PriceQuotation(issuer, stockName, value);
    } ;
  }

  public static JsonLfDecoder<PriceQuotation> jsonDecoder() {
    return JsonLfDecoders.record(Arrays.asList("issuer", "stockName", "value"), name -> {
          switch (name) {
            case "issuer": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(0, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.party);
            case "stockName": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(1, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.text);
            case "value": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(2, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.int64);
            default: return null;
          }
        }
        , (Object[] args) -> new PriceQuotation(JsonLfDecoders.cast(args[0]), JsonLfDecoders.cast(args[1]), JsonLfDecoders.cast(args[2])));
  }

  public static PriceQuotation fromJson(String json) throws JsonLfDecoder.Error {
    return jsonDecoder().decode(new JsonLfReader(json));
  }

  public JsonLfEncoder jsonEncoder() {
    return JsonLfEncoders.record(
        JsonLfEncoders.Field.of("issuer", apply(JsonLfEncoders::party, issuer)),
        JsonLfEncoders.Field.of("stockName", apply(JsonLfEncoders::text, stockName)),
        JsonLfEncoders.Field.of("value", apply(JsonLfEncoders::int64, value)));
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
    if (!(object instanceof PriceQuotation)) {
      return false;
    }
    PriceQuotation other = (PriceQuotation) object;
    return Objects.equals(this.issuer, other.issuer) &&
        Objects.equals(this.stockName, other.stockName) && Objects.equals(this.value, other.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.issuer, this.stockName, this.value);
  }

  @Override
  public String toString() {
    return String.format("examples.codegen.stockexchange.PriceQuotation(%s, %s, %s)", this.issuer,
        this.stockName, this.value);
  }

  public static final class ContractId extends com.daml.ledger.javaapi.data.codegen.ContractId<PriceQuotation> implements Exercises<ExerciseCommand> {
    public ContractId(String contractId) {
      super(contractId);
    }

    @Override
    protected ContractTypeCompanion<? extends com.daml.ledger.javaapi.data.codegen.Contract<ContractId, ?>, ContractId, PriceQuotation, ?> getCompanion(
        ) {
      return COMPANION;
    }

    public static ContractId fromContractId(
        com.daml.ledger.javaapi.data.codegen.ContractId<PriceQuotation> contractId) {
      return COMPANION.toContractId(contractId);
    }
  }

  public static class Contract extends com.daml.ledger.javaapi.data.codegen.Contract<ContractId, PriceQuotation> {
    public Contract(ContractId id, PriceQuotation data, Optional<String> agreementText,
        Set<String> signatories, Set<String> observers) {
      super(id, data, agreementText, signatories, observers);
    }

    @Override
    protected ContractCompanion<Contract, ContractId, PriceQuotation> getCompanion() {
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
    default Update<Exercised<PriceQuotation>> exercisePriceQuotation_Fetch(
        PriceQuotation_Fetch arg) {
      return makeExerciseCmd(CHOICE_PriceQuotation_Fetch, arg);
    }

    default Update<Exercised<PriceQuotation>> exercisePriceQuotation_Fetch(String fetcher) {
      return exercisePriceQuotation_Fetch(new PriceQuotation_Fetch(fetcher));
    }

    default Update<Exercised<Unit>> exerciseArchive(
        examples.codegen.da.internal.template.Archive arg) {
      return makeExerciseCmd(CHOICE_Archive, arg);
    }

    default Update<Exercised<Unit>> exerciseArchive() {
      return exerciseArchive(new examples.codegen.da.internal.template.Archive());
    }
  }

  public static final class CreateAnd extends com.daml.ledger.javaapi.data.codegen.CreateAnd implements Exercises<CreateAndExerciseCommand> {
    CreateAnd(Template createArguments) {
      super(createArguments);
    }

    @Override
    protected ContractTypeCompanion<? extends com.daml.ledger.javaapi.data.codegen.Contract<ContractId, ?>, ContractId, PriceQuotation, ?> getCompanion(
        ) {
      return COMPANION;
    }
  }
}
