# Credit Risk Scoring

Java credit risk scoring system for a fintech lender, built on the Statlog German Credit dataset (1,000 applicants). Positive class is always **bad** (default).

## Requirements

- Java 17+ (JAR is compiled with `maven.compiler.release=17`)
- Maven 3.9+
- Dataset at `data/credit-g.arff` (Statlog German Credit)

## Build

```bash
mvn clean verify
```

## Train

Loads `data/credit-g.arff` into the H2 file database (`loan_applications`), profiles the table, runs stratified 10-fold CV (seed 42) for Logistic, J48, RandomForest (200 trees) and NaiveBayes, writes `docs/evaluation-report.md`, and saves the Logistic champion to `models/credit-logit.model`.

```bash
mvn -DskipTests package
java -jar target/credit-risk-scoring-1.0.0-SNAPSHOT.jar train
```

## Serve

```bash
java -jar target/credit-risk-scoring-1.0.0-SNAPSHOT.jar serve
# or simply:
java -jar target/credit-risk-scoring-1.0.0-SNAPSHOT.jar
```

### Sample score request

```bash
curl -s -X POST http://localhost:8080/api/v1/score \
  -H 'Content-Type: application/json' \
  -d '{
    "checking_status": "<0",
    "duration": 6,
    "credit_history": "critical/other existing credit",
    "purpose": "radio/tv",
    "credit_amount": 1169,
    "savings_status": "no known savings",
    "employment": ">=7",
    "installment_commitment": 4,
    "personal_status": "male single",
    "other_parties": "none",
    "residence_since": 4,
    "property_magnitude": "real estate",
    "age": 67,
    "other_payment_plans": "none",
    "housing": "own",
    "existing_credits": 2,
    "job": "skilled",
    "num_dependents": 1,
    "own_telephone": "yes",
    "foreign_worker": "yes"
  }'
```

Model metadata:

```bash
curl -s http://localhost:8080/api/v1/model
```

Decision thresholds (`approve` / `refer` / `decline`) are in `src/main/resources/application.yml`.

## Project layout

| Path | Role |
| --- | --- |
| `data/credit-g.arff` | Source dataset (loaded once into H2) |
| `models/credit-logit.model` | Serialized champion `FilteredClassifier` |
| `docs/data-profile.txt` | Tablesaw profile |
| `docs/evaluation-report.md` | CV comparison table (generated) |
| `docs/dependency-tree.txt` | `mvn dependency:tree` output |

Integration points for enterprise systems are stubbed: `CreditBureauLookup` and `LoanApplicationEvents` (in-memory; no Kafka on the classpath).

## How Maven manages dependencies here

- **Coordinates** — each library is identified by `groupId:artifactId:version` (for example `nz.ac.waikato.cms.weka:weka-stable:3.8.6`).
- **Transitive dependencies** — Maven pulls libraries that our dependencies need (Spring Boot → Jackson, Tomcat; ND4J → native binaries). The resolved graph is in `docs/dependency-tree.txt`.
- **Scopes** — `compile` (default) ships in the JAR; `runtime` (H2) is needed to run but not to compile against; `test` (JUnit) is only on the test classpath; Spring Boot’s BOM is imported as `pom`/`import` in `dependencyManagement`.
- **dependencyManagement** — versions are declared once under `<properties>` and locked in `<dependencyManagement>` so child dependencies and the Spring Boot BOM stay aligned without repeating versions on every `<dependency>`.
- **Exclusions** — Weka’s transitive `slf4j-api` is excluded so Spring Boot’s logging stack remains the single SLF4J binding (avoids duplicate logging bindings at runtime).
