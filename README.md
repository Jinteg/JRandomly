# JRandomly

Reproducible, random and locale-aware test data generator for Java 21+.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)]()
[![MvnRepository](https://badges.mvnrepository.com/badge/de.jinteg.jrandomly/jrandomly-testdata/badge.svg?label=MvnRepository&color=green)](https://mvnrepository.com/artifact/de.jinteg.jrandomly/jrandomly-testdata)
---

## Features

- **Reproducible** – Every test run can be replayed with the same seed, `runStartTime` and locale
- **Parallel-safe** – Scoped instances (`randomly("myScope")`) produce deterministic results even in parallel tests
- **Locale-aware** – Domain catalogs (finance, person, company) support locale overrides at domain or method level
- **Fluent API** – Discoverable modules: `r.dateTime()`, `r.text()`, `r.id()`, `r.finance()`, `r.person()`, `r.company()`, `r.maybe()`
- **Zero dependencies** – Pure Java 21, no external runtime dependencies
- **Replay info** – Copy-paste friendly CLI args to reproduce any run

## Getting Started

### Maven

``` xml
<dependency>
    <groupId>de.jinteg.jrandomly</groupId>
    <artifactId>jrandomly-testdata</artifactId>
    <version>0.3.0</version>
    <scope>test</scope>
</dependency>
```

### Gradle

``` groovy
testImplementation 'de.jinteg.jrandomly:jrandomly-testdata:0.3.0'
```

## Quick Start

``` java
import de.jinteg.randomly.JRandomly;

// Create a scoped instance: reproducible and safe for parallel tests
JRandomly r = JRandomly.randomly("OrderTest#createsOrder");

// Core utilities
int age      = r.intBetween(18, 65);
boolean flag = r.bool();

// Date/Time generation (anchored to runStartTime, not wall-clock)
LocalDate birthday = r.dateTime().localDateBefore(365 * 30);
Instant   dueAt    = r.dateTime().instantInFuture(1, 30);

// Text and ids
String productName = r.text().compoundName();
String orderId     = r.id().prefixedId("ORD-", 12);

// Domain modules (locale-aware catalogs)
StockPick   stock    = r.finance().stock(Locale.US);
PersonPick  customer = r.person().data(Locale.GERMANY);
CompanyPick supplier = r.company().companyByName("SAP", Locale.GERMANY);

// Maybe – nullable test data with configurable absence probability
String nickname = r.maybeText("FooBar").orElse("");
```

## Reproducibility

JRandomly writes one line of replay information per instance to `target/jrandomly-replay.txt`
(configurable via `jrandomly.replayFile`, or `off`). To reproduce a failing test run, copy the
values of that run and pass them as system properties:

``` bash
mvn test -Djrandomly.seed=123456789 \
-Djrandomly.runStartTime=2026-02-17T10:15:30Z \
-Djrandomly.locale=de-DE \
-Djrandomly.version=0.3.0
```

`jrandomly.version` does not change the generated values. It logs a warning if the run uses
a different JRandomly version than the recorded one, because values may then differ.

Or retrieve replay info programmatically:

``` java
JRandomly r = JRandomly.randomly();
System.out.println(r.replayInfo());
// Output: -Djrandomly.seed=... -Djrandomly.runStartTime=... -Djrandomly.locale=...
//         -Djrandomly.maybeRate=... -Djrandomly.version=...
```

### What is guaranteed

Within the **same JRandomly version**, the same seed, scope, `runStartTime`, locale and
`maybeRate` produce the same values on every run, JVM and operating system.

Across versions, values for an existing seed may change when catalog data or selection
logic changes. Such changes are listed in the [CHANGELOG](CHANGELOG.md) under
**Reproducibility**. To replay a run, use the JRandomly version of that run.

For stable results:

- use scoped instances (`randomly("myScope")`) in parallel tests,
- pass ordered collections (`List`, `LinkedHashSet`, `TreeSet`) to `elementOf` / `elementsOf`,
- use `dateTime(ZoneId)` if values must not depend on the system time zone.

Details: [ADR-0008](docs/adr/adr-0008-jrandomly-reproducibility-contract.md).

## Configuration

| System Property          | Env Variable               | Default               | Description                     |
|--------------------------|----------------------------|-----------------------|---------------------------------|
| `jrandomly.seed`         | `JRANDOMLY_SEED`           | auto (entropy-based)  | Root seed for RNG               |
| `jrandomly.runStartTime` | `JRANDOMLY_RUN_START_TIME` | `Instant.now()`       | Time anchor for date generators |
| `jrandomly.locale`       | `JRANDOMLY_LOCALE`         | `Locale.getDefault()` | Default locale for catalogs     |
| `jrandomly.version`      | `JRANDOMLY_VERSION`        | –                     | Version a replay was recorded with; logs a warning if it differs from the running version |
| `jrandomly.replayFile`   | `JRANDOMLY_REPLAY_FILE`    | `target/jrandomly-replay.txt` | Replay file location, or `off` to disable it (e.g. `build/jrandomly-replay.txt` for Gradle) |

**Precedence:** Builder API > System Property > Environment Variable > Default

## Parallel-Safe Scoping

Use scoped instances for deterministic results in parallel test execution:

``` java
JRandomly r1 = JRandomly.randomly("orderTest");
JRandomly r2 = JRandomly.randomly("userTest");
// r1 and r2 produce independent, reproducible streams
```

A `JRandomly` instance is **not thread-safe**. Create one instance per test (or per thread)
instead of sharing an instance between threads. Unscoped `randomly()` instances depend on
their creation order and are therefore not reproducible in parallel runs.

## Stable Values with Forks

All values of one instance come from one random stream. If you add a call, every value
generated after it changes. Use named forks for groups of values that must stay stable:

``` java
JRandomly r = JRandomly.randomly("OrderTest#createsOrder");

PersonPick customer = r.fork("customer").person().data();
StockPick  stock    = r.fork("stock").finance().stock();
// Adding calls on r or on other forks does not change customer or stock.
```

- A fork's values depend only on the parent's seed and the fork name; forking does not
  consume values from the parent.
- The same name always yields the same stream; forks can be nested
  (`r.fork("order").fork("items")`).
- Locale, `runStartTime` and `maybeRate` are inherited.
- For multithreaded test code, hand one fork per thread to the workers
  (`r.fork("worker-" + i)`) – each stays reproducible.

## Modules

| Module       | Access         | Examples                                               |
|--------------|----------------|--------------------------------------------------------|
| **Core**     | `r.*`          | `intBetween()`, `bool()`, `enumValue()`, `elementOf()` |
| **DateTime** | `r.dateTime()` | `pastDate()`, `futureInstant()`                        |
| **Text**     | `r.text()`     | String generation utilities                            |
| **Id**       | `r.id()`       | Identifier generation                                  |
| **Maybe**    | `r.maybe()`    | Nullable/optional test data                            |
| **Finance**  | `r.finance()`  | `stock()`, `stockBySymbol("AAPL")`, `cryptoAsset()`, `currencyCode()` |
| **Person**   | `r.person()`   | `data()`, `personById(7)`, `gender()`                  |
| **Company**  | `r.company()`  | `data()`, `companyByName("SAP")`                       |

Domain catalogs are available for `de`, `en`, `ja` and `tr` (person, company) and `de`, `en`
(finance stocks, text). An unsupported locale fails fast instead of silently falling back.

Person and company data is fictitious test data. Company catalogs use well-known company names
for readability; addresses, websites, email addresses, phone numbers and VAT ids are made up.

## Requirements

- **Java 21** or higher

## License

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).

Copyright © 2026 [Jinteg®](https://www.jinteg.de)

