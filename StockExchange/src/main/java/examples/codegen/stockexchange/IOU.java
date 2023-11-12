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

public final class IOU extends Template {
  public static final Identifier TEMPLATE_ID = new Identifier("33e53a5c42e4c0794de777b3c01b98906e72743b0bba101437bf35e1be50000f", "StockExchange", "IOU");

  public static final Choice<IOU, IOU_Transfer, Unit> CHOICE_IOU_Transfer = 
      Choice.create("IOU_Transfer", value$ -> value$.toValue(), value$ ->
        IOU_Transfer.valueDecoder().decode(value$), value$ -> PrimitiveValueDecoders.fromUnit
        .decode(value$));

  public static final Choice<IOU, examples.codegen.da.internal.template.Archive, Unit> CHOICE_Archive = 
      Choice.create("Archive", value$ -> value$.toValue(), value$ ->
        examples.codegen.da.internal.template.Archive.valueDecoder().decode(value$), value$ ->
        PrimitiveValueDecoders.fromUnit.decode(value$));

  public static final ContractCompanion.WithoutKey<Contract, ContractId, IOU> COMPANION = 
      new ContractCompanion.WithoutKey<>("examples.codegen.stockexchange.IOU", TEMPLATE_ID,
        ContractId::new, v -> IOU.templateValueDecoder().decode(v), IOU::fromJson, Contract::new,
        List.of(CHOICE_IOU_Transfer, CHOICE_Archive));

  public final String issuer;

  public final String owner;

  public final Long value;

  public IOU(String issuer, String owner, Long value) {
    this.issuer = issuer;
    this.owner = owner;
    this.value = value;
  }

  @Override
  public Update<Created<ContractId>> create() {
    return new Update.CreateUpdate<ContractId, Created<ContractId>>(new CreateCommand(IOU.TEMPLATE_ID, this.toValue()), x -> x, ContractId::new);
  }

  /**
   * @deprecated since Daml 2.3.0; use {@code createAnd().exerciseIOU_Transfer} instead
   */
  @Deprecated
  public Update<Exercised<Unit>> createAndExerciseIOU_Transfer(IOU_Transfer arg) {
    return createAnd().exerciseIOU_Transfer(arg);
  }

  /**
   * @deprecated since Daml 2.3.0; use {@code createAnd().exerciseIOU_Transfer} instead
   */
  @Deprecated
  public Update<Exercised<Unit>> createAndExerciseIOU_Transfer(String target, Long amount) {
    return createAndExerciseIOU_Transfer(new IOU_Transfer(target, amount));
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

  public static Update<Created<ContractId>> create(String issuer, String owner, Long value) {
    return new IOU(issuer, owner, value).create();
  }

  @Override
  public CreateAnd createAnd() {
    return new CreateAnd(this);
  }

  @Override
  protected ContractCompanion.WithoutKey<Contract, ContractId, IOU> getCompanion() {
    return COMPANION;
  }

  /**
   * @deprecated since Daml 2.5.0; use {@code valueDecoder} instead
   */
  @Deprecated
  public static IOU fromValue(Value value$) throws IllegalArgumentException {
    return valueDecoder().decode(value$);
  }

  public static ValueDecoder<IOU> valueDecoder() throws IllegalArgumentException {
    return ContractCompanion.valueDecoder(COMPANION);
  }

  public DamlRecord toValue() {
    ArrayList<DamlRecord.Field> fields = new ArrayList<DamlRecord.Field>(3);
    fields.add(new DamlRecord.Field("issuer", new Party(this.issuer)));
    fields.add(new DamlRecord.Field("owner", new Party(this.owner)));
    fields.add(new DamlRecord.Field("value", new Int64(this.value)));
    return new DamlRecord(fields);
  }

  private static ValueDecoder<IOU> templateValueDecoder() throws IllegalArgumentException {
    return value$ -> {
      Value recordValue$ = value$;
      List<DamlRecord.Field> fields$ = PrimitiveValueDecoders.recordCheck(3,0, recordValue$);
      String issuer = PrimitiveValueDecoders.fromParty.decode(fields$.get(0).getValue());
      String owner = PrimitiveValueDecoders.fromParty.decode(fields$.get(1).getValue());
      Long value = PrimitiveValueDecoders.fromInt64.decode(fields$.get(2).getValue());
      return new IOU(issuer, owner, value);
    } ;
  }

  public static JsonLfDecoder<IOU> jsonDecoder() {
    return JsonLfDecoders.record(Arrays.asList("issuer", "owner", "value"), name -> {
          switch (name) {
            case "issuer": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(0, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.party);
            case "owner": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(1, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.party);
            case "value": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(2, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.int64);
            default: return null;
          }
        }
        , (Object[] args) -> new IOU(JsonLfDecoders.cast(args[0]), JsonLfDecoders.cast(args[1]), JsonLfDecoders.cast(args[2])));
  }

  public static IOU fromJson(String json) throws JsonLfDecoder.Error {
    return jsonDecoder().decode(new JsonLfReader(json));
  }

  public JsonLfEncoder jsonEncoder() {
    return JsonLfEncoders.record(
        JsonLfEncoders.Field.of("issuer", apply(JsonLfEncoders::party, issuer)),
        JsonLfEncoders.Field.of("owner", apply(JsonLfEncoders::party, owner)),
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
    if (!(object instanceof IOU)) {
      return false;
    }
    IOU other = (IOU) object;
    return Objects.equals(this.issuer, other.issuer) && Objects.equals(this.owner, other.owner) &&
        Objects.equals(this.value, other.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.issuer, this.owner, this.value);
  }

  @Override
  public String toString() {
    return String.format("examples.codegen.stockexchange.IOU(%s, %s, %s)", this.issuer, this.owner,
        this.value);
  }

  public static final class ContractId extends com.daml.ledger.javaapi.data.codegen.ContractId<IOU> implements Exercises<ExerciseCommand> {
    public ContractId(String contractId) {
      super(contractId);
    }

    @Override
    protected ContractTypeCompanion<? extends com.daml.ledger.javaapi.data.codegen.Contract<ContractId, ?>, ContractId, IOU, ?> getCompanion(
        ) {
      return COMPANION;
    }

    public static ContractId fromContractId(
        com.daml.ledger.javaapi.data.codegen.ContractId<IOU> contractId) {
      return COMPANION.toContractId(contractId);
    }
  }

  public static class Contract extends com.daml.ledger.javaapi.data.codegen.Contract<ContractId, IOU> {
    public Contract(ContractId id, IOU data, Optional<String> agreementText,
        Set<String> signatories, Set<String> observers) {
      super(id, data, agreementText, signatories, observers);
    }

    @Override
    protected ContractCompanion<Contract, ContractId, IOU> getCompanion() {
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
    default Update<Exercised<Unit>> exerciseIOU_Transfer(IOU_Transfer arg) {
      return makeExerciseCmd(CHOICE_IOU_Transfer, arg);
    }

    default Update<Exercised<Unit>> exerciseIOU_Transfer(String target, Long amount) {
      return exerciseIOU_Transfer(new IOU_Transfer(target, amount));
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
    protected ContractTypeCompanion<? extends com.daml.ledger.javaapi.data.codegen.Contract<ContractId, ?>, ContractId, IOU, ?> getCompanion(
        ) {
      return COMPANION;
    }
  }
}
