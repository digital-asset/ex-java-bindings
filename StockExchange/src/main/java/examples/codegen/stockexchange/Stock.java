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
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class Stock extends Template {
  public static final Identifier TEMPLATE_ID = new Identifier("33e53a5c42e4c0794de777b3c01b98906e72743b0bba101437bf35e1be50000f", "StockExchange", "Stock");

  public static final Choice<Stock, Stock_Transfer, Unit> CHOICE_Stock_Transfer = 
      Choice.create("Stock_Transfer", value$ -> value$.toValue(), value$ ->
        Stock_Transfer.valueDecoder().decode(value$), value$ -> PrimitiveValueDecoders.fromUnit
        .decode(value$));

  public static final Choice<Stock, examples.codegen.da.internal.template.Archive, Unit> CHOICE_Archive = 
      Choice.create("Archive", value$ -> value$.toValue(), value$ ->
        examples.codegen.da.internal.template.Archive.valueDecoder().decode(value$), value$ ->
        PrimitiveValueDecoders.fromUnit.decode(value$));

  public static final ContractCompanion.WithoutKey<Contract, ContractId, Stock> COMPANION = 
      new ContractCompanion.WithoutKey<>("examples.codegen.stockexchange.Stock", TEMPLATE_ID,
        ContractId::new, v -> Stock.templateValueDecoder().decode(v), Stock::fromJson,
        Contract::new, List.of(CHOICE_Stock_Transfer, CHOICE_Archive));

  public final String issuer;

  public final String owner;

  public final String stockName;

  public Stock(String issuer, String owner, String stockName) {
    this.issuer = issuer;
    this.owner = owner;
    this.stockName = stockName;
  }

  @Override
  public Update<Created<ContractId>> create() {
    return new Update.CreateUpdate<ContractId, Created<ContractId>>(new CreateCommand(Stock.TEMPLATE_ID, this.toValue()), x -> x, ContractId::new);
  }

  /**
   * @deprecated since Daml 2.3.0; use {@code createAnd().exerciseStock_Transfer} instead
   */
  @Deprecated
  public Update<Exercised<Unit>> createAndExerciseStock_Transfer(Stock_Transfer arg) {
    return createAnd().exerciseStock_Transfer(arg);
  }

  /**
   * @deprecated since Daml 2.3.0; use {@code createAnd().exerciseStock_Transfer} instead
   */
  @Deprecated
  public Update<Exercised<Unit>> createAndExerciseStock_Transfer(String newOwner) {
    return createAndExerciseStock_Transfer(new Stock_Transfer(newOwner));
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

  public static Update<Created<ContractId>> create(String issuer, String owner, String stockName) {
    return new Stock(issuer, owner, stockName).create();
  }

  @Override
  public CreateAnd createAnd() {
    return new CreateAnd(this);
  }

  @Override
  protected ContractCompanion.WithoutKey<Contract, ContractId, Stock> getCompanion() {
    return COMPANION;
  }

  /**
   * @deprecated since Daml 2.5.0; use {@code valueDecoder} instead
   */
  @Deprecated
  public static Stock fromValue(Value value$) throws IllegalArgumentException {
    return valueDecoder().decode(value$);
  }

  public static ValueDecoder<Stock> valueDecoder() throws IllegalArgumentException {
    return ContractCompanion.valueDecoder(COMPANION);
  }

  public DamlRecord toValue() {
    ArrayList<DamlRecord.Field> fields = new ArrayList<DamlRecord.Field>(3);
    fields.add(new DamlRecord.Field("issuer", new Party(this.issuer)));
    fields.add(new DamlRecord.Field("owner", new Party(this.owner)));
    fields.add(new DamlRecord.Field("stockName", new Text(this.stockName)));
    return new DamlRecord(fields);
  }

  private static ValueDecoder<Stock> templateValueDecoder() throws IllegalArgumentException {
    return value$ -> {
      Value recordValue$ = value$;
      List<DamlRecord.Field> fields$ = PrimitiveValueDecoders.recordCheck(3,0, recordValue$);
      String issuer = PrimitiveValueDecoders.fromParty.decode(fields$.get(0).getValue());
      String owner = PrimitiveValueDecoders.fromParty.decode(fields$.get(1).getValue());
      String stockName = PrimitiveValueDecoders.fromText.decode(fields$.get(2).getValue());
      return new Stock(issuer, owner, stockName);
    } ;
  }

  public static JsonLfDecoder<Stock> jsonDecoder() {
    return JsonLfDecoders.record(Arrays.asList("issuer", "owner", "stockName"), name -> {
          switch (name) {
            case "issuer": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(0, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.party);
            case "owner": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(1, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.party);
            case "stockName": return com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.JavaArg.at(2, com.daml.ledger.javaapi.data.codegen.json.JsonLfDecoders.text);
            default: return null;
          }
        }
        , (Object[] args) -> new Stock(JsonLfDecoders.cast(args[0]), JsonLfDecoders.cast(args[1]), JsonLfDecoders.cast(args[2])));
  }

  public static Stock fromJson(String json) throws JsonLfDecoder.Error {
    return jsonDecoder().decode(new JsonLfReader(json));
  }

  public JsonLfEncoder jsonEncoder() {
    return JsonLfEncoders.record(
        JsonLfEncoders.Field.of("issuer", apply(JsonLfEncoders::party, issuer)),
        JsonLfEncoders.Field.of("owner", apply(JsonLfEncoders::party, owner)),
        JsonLfEncoders.Field.of("stockName", apply(JsonLfEncoders::text, stockName)));
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
    if (!(object instanceof Stock)) {
      return false;
    }
    Stock other = (Stock) object;
    return Objects.equals(this.issuer, other.issuer) && Objects.equals(this.owner, other.owner) &&
        Objects.equals(this.stockName, other.stockName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.issuer, this.owner, this.stockName);
  }

  @Override
  public String toString() {
    return String.format("examples.codegen.stockexchange.Stock(%s, %s, %s)", this.issuer,
        this.owner, this.stockName);
  }

  public static final class ContractId extends com.daml.ledger.javaapi.data.codegen.ContractId<Stock> implements Exercises<ExerciseCommand> {
    public ContractId(String contractId) {
      super(contractId);
    }

    @Override
    protected ContractTypeCompanion<? extends com.daml.ledger.javaapi.data.codegen.Contract<ContractId, ?>, ContractId, Stock, ?> getCompanion(
        ) {
      return COMPANION;
    }

    public static ContractId fromContractId(
        com.daml.ledger.javaapi.data.codegen.ContractId<Stock> contractId) {
      return COMPANION.toContractId(contractId);
    }
  }

  public static class Contract extends com.daml.ledger.javaapi.data.codegen.Contract<ContractId, Stock> {
    public Contract(ContractId id, Stock data, Optional<String> agreementText,
        Set<String> signatories, Set<String> observers) {
      super(id, data, agreementText, signatories, observers);
    }

    @Override
    protected ContractCompanion<Contract, ContractId, Stock> getCompanion() {
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
    default Update<Exercised<Unit>> exerciseStock_Transfer(Stock_Transfer arg) {
      return makeExerciseCmd(CHOICE_Stock_Transfer, arg);
    }

    default Update<Exercised<Unit>> exerciseStock_Transfer(String newOwner) {
      return exerciseStock_Transfer(new Stock_Transfer(newOwner));
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
    protected ContractTypeCompanion<? extends com.daml.ledger.javaapi.data.codegen.Contract<ContractId, ?>, ContractId, Stock, ?> getCompanion(
        ) {
      return COMPANION;
    }
  }
}
