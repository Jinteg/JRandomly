# JRandomly

Reproducible, random and locale-aware test data generator for Java 21+.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)]()
[![MvnRepository](https://badges.mvnrepository.com/badge/de.jinteg.jrandomly/jrandomly-testdata/badge.svg?label=MvnRepository&color=green)](https://mvnrepository.com/artifact/de.jinteg.jrandomly/jrandomly-testdata)
---

## Features

- **Reproducible** – Every test run can be replayed with the same seed, `runStartTime` and locale
- **Parallel-safe** – Scoped instances (`randomly("myScope")`) produce deterministic results even in parallel tests
- **Locale-aware** – Domain catalogs (e.g. finance) support locale overrides at domain or method level
- **Fluent API** – Discoverable modules: `r.dateTime()`, `r.text()`, `r.id()`, `r.finance()`, `r.maybe()`
- **Zero dependencies** – Pure Java 21, no external runtime dependencies
- **Replay info** – Copy-paste friendly CLI args to reproduce any run

## Getting Started

### Maven

``` xml
<dependency>
    <groupId>de.jinteg.jrandomly</groupId>
    <artifactId>jrandomly-testdata</artifactId>
    <version>0.2.0</version>
    <scope>test</scope>
</dependency>
```

### Gradle

``` groovy
testImplementation 'de.jinteg.jrandomly:jrandomly-testdata:0.2.0'
```

## Quick Start

``` java
import de.jinteg.randomly.JRandomly;

// Create an instance with defaults (auto-seed, system locale)
JRandomly r = JRandomly.randomly();

// Core utilities
int age      = r.intBetween(18, 65);
boolean flag = r.bool();

// Date/Time generation (anchored to runStartTime, not wall-clock)
LocalDate birthday  = r.dateTime().pastDate();
Instant   timestamp = r.dateTime().futureInstant();

// Domain modules
String ticker = r.finance().stockSymbol();

// Maybe – nullable test data with configurable absence probability
String nickname = r.maybeText("FooBar").orElse("");
```

## Reproducibility

JRandomly logs replay information at instance creation. To reproduce a failing test run,
copy the logged values and pass them as system properties:

``` bash
mvn test -Djrandomly.seed=123456789 \
-Djrandomly.runStartTime=2026-02-17T10:15:30Z \
-Djrandomly.locale=de-DE
```

Or retrieve replay info programmatically:

``` java
JRandomly r = JRandomly.randomly();
System.out.println(r.replayInfo());
// Output: -Djrandomly.seed=... -Djrandomly.runStartTime=... -Djrandomly.locale=...
```

## Configuration

| System Property          | Env Variable               | Default               | Description                     |
|--------------------------|----------------------------|-----------------------|---------------------------------|
| `jrandomly.seed`         | `JRANDOMLY_SEED`           | auto (entropy-based)  | Root seed for RNG               |
| `jrandomly.runStartTime` | `JRANDOMLY_RUN_START_TIME` | `Instant.now()`       | Time anchor for date generators |
| `jrandomly.locale`       | `JRANDOMLY_LOCALE`         | `Locale.getDefault()` | Default locale for catalogs     |

**Precedence:** Builder API > System Property > Environment Variable > Default

## Parallel-Safe Scoping

Use scoped instances for deterministic results in parallel test execution:

``` java
JRandomly r1 = JRandomly.randomly("orderTest");
JRandomly r2 = JRandomly.randomly("userTest");
// r1 and r2 produce independent, reproducible streams
```

## Modules

| Module       | Access         | Examples                                               |
|--------------|----------------|--------------------------------------------------------|
| **Core**     | `r.*`          | `intBetween()`, `bool()`, `enumValue()`, `elementOf()` |
| **DateTime** | `r.dateTime()` | `pastDate()`, `futureInstant()`                        |
| **Text**     | `r.text()`     | String generation utilities                            |
| **Id**       | `r.id()`       | Identifier generation                                  |
| **Maybe**    | `r.maybe()`    | Nullable/optional test data                            |
| **Finance**  | `r.finance()`  | `stockSymbol()`, locale-aware catalogs                 |

## Requirements

- **Java 21** or higher

## License

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).

Copyright © 2026 [Jinteg®](https://www.jinteg.de)

