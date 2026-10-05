# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/),
and this project adheres to [Semantic Versioning](https://semver.org/).

Changes that alter generated values for an existing seed are listed under
**Reproducibility** (see [ADR-0008](docs/adr/adr-0008-jrandomly-reproducibility-contract.md)).

## [Unreleased]

### Added

- `fork(String name)`: named, independent random streams within an instance. Values of a
  fork depend only on the parent's seed and the name, so they stay stable when other calls
  are added; one fork per thread makes multithreaded test code reproducible (ADR-0002)
- `Automatic-Module-Name: de.jinteg.randomly` in the JAR manifest for users on the module path

### Fixed

- The replay file header could name the caller of another thread in parallel test runs
- README Quick Start used methods that do not exist (`pastDate()`, `futureInstant()`)
- Javadoc of `dateTime().instant()`, `localDate()` and `localDateTime()` described random
  values; they return the `runStartTime` anchor ("now" / "today" of the test run)

### Documentation

- Thread safety of `JRandomly` instances documented (one instance per test or thread)

## [0.3.0] - 2026-10-05

### Added

- Person domain `person()` with localized catalogs (de, en, ja, tr) and gender registry
- Deterministic lookups: `finance().stockBySymbol()`, `finance().cryptoAssetBySymbol()`,
  `person().personById()` (ADR-0007)
- ADR-0008 defining the scope of the reproducibility contract
- `ReproducibilityContractTest` pinning generated values for fixed inputs
- Company domain `company()` with localized catalogs (de, en, ja, tr) and
  `companyByName()` lookup
- `replayInfo()` and the replay file header include the JRandomly version; replaying with a
  different version (`jrandomly.version` / `JRANDOMLY_VERSION`) logs a warning

### Fixed

- Numbered catalogs with several key groups (stock catalogs) lost entries: only 60 of 150
  German and 101 of 151 English stocks were reachable; `stockBySymbol("AAPL")` failed
- UTF-8 encoding of German text, stock and crypto catalog entries
- Catalog loader did not close its reader (resource leak)

### Reproducibility

- `finance().stock()` and `finance().stockSymbol()` return different values for the
  locales `de` and `en`, because all catalog entries are now loaded and ordered by key
  prefix, then by number
- `finance().currency()`, `currencyCode()` and `currencySymbol()` select from a fixed list
  of 36 ISO 4217 currencies instead of the JDK's currency set, whose order and content
  depend on the JDK version; `currencySymbol()` renders symbols for the configured locale
  instead of the JVM default locale
- Values that contained broken umlauts or other non-ASCII characters are now returned
  correctly encoded (text, stock and crypto catalogs)

## [0.2.0] - 2026-03-08

### Added

- `finance().cryptoAsset()` / `cryptoAsset(String quoteCurrencyCode)` returning
  `CryptoAssetPick`; without an explicit currency, the quote currency is derived from the
  configured locale (fallback USD)
- `finance().cryptoPairSymbol()` / `cryptoPairSymbol(String quoteCurrencyCode)`, e.g. `BTC-EUR`
- Catalog of 10 crypto assets and sample FX rates vs. USD for 10 currencies
  (approximate test values, not live rates)
- `StockPick` fields `currencyCode`, `isin` and `mic`
- Stock catalogs extended to 150 German (DAX, MDAX, SDAX) and 151 US (Nasdaq-100, NYSE) stocks
- `intWithExactDigits()`, `intWithMaxDigits()`, `longWithExactDigits()`, `longWithMaxDigits()`
- `id().intIdWithExactDigits()`, `intIdWithMaxDigits()`, `longIdWithExactDigits()`,
  `longIdWithMaxDigits()`, `key()` and `numericKey()`

### Changed

- **BREAKING:** `StockEntry` renamed to `StockPick` (domain records use the `*Pick` suffix)
- **BREAKING:** stock accessor `name()` renamed to `companyName()`
- Stock market caps are parsed with suffixes (`K`, `M`, `B`, `T`)

### Known issues

- Only 60 of 150 German and 101 of 151 US stocks are reachable via `finance().stock()`
  (fixed in the next release)
- `finance().currency*()` depends on the JDK's currency set and the JVM default locale
  (fixed in the next release)

## [0.1.0] - 2026-02-27

### Added

- Reproducible random data generation with seed, `runStartTime` and locale
- Parallel-safe scoped instances via `randomly("myScope")`
- Locale-aware domain catalogs (e.g. finance)
- Fluent API modules: `dateTime()`, `text()`, `id()`, `finance()`, `maybe()`, `uniqueIndices()`
- Zero external runtime dependencies – pure Java 21
- Replay info with copy-paste friendly CLI args

[Unreleased]: https://github.com/Jinteg/JRandomly/compare/v0.3.0...develop
[0.3.0]: https://github.com/Jinteg/JRandomly/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/Jinteg/JRandomly/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/Jinteg/JRandomly/releases/tag/v0.1.0

