# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/),
and this project adheres to [Semantic Versioning](https://semver.org/).

Changes that alter generated values for an existing seed are listed under
**Reproducibility** (see [ADR-0008](docs/adr/adr-0008-jrandomly-reproducibility-contract.md)).

## [Unreleased]

### Added

- Person domain `person()` with localized catalogs (de, en, ja, tr) and gender registry
- Deterministic lookups: `finance().stockBySymbol()`, `finance().cryptoAssetBySymbol()`,
  `person().personById()` (ADR-0007)
- ADR-0008 defining the scope of the reproducibility contract
- `ReproducibilityContractTest` pinning generated values for fixed inputs

### Fixed

- Numbered catalogs with several key groups (stock catalogs) lost entries: only 60 of 150
  German and 101 of 151 English stocks were reachable; `stockBySymbol("AAPL")` failed
- `person().gender()` returned different values for the same seed across JVM runs
- UTF-8 encoding of German text, stock and crypto catalog entries
- Japanese person catalog now uses localized marital status values
- Typo in stock entry `Münchener Rück SE`
- SpotBugs findings in catalog loader and replay file writer

### Reproducibility

- `finance().stock()` and `finance().stockSymbol()` return different values for the
  locales `de` and `en`, because all catalog entries are now loaded and ordered by key
  prefix, then by number
- Values that contained broken umlauts or other non-ASCII characters are now returned
  correctly encoded (text, stock and crypto catalogs)

## [0.1.0] - 2026-02-27

### Added

- Reproducible random data generation with seed, `runStartTime` and locale
- Parallel-safe scoped instances via `randomly("myScope")`
- Locale-aware domain catalogs (e.g. finance)
- Fluent API modules: `dateTime()`, `text()`, `id()`, `finance()`, `maybe()`, `uniqueIndices()`
- Zero external runtime dependencies – pure Java 21
- Replay info with copy-paste friendly CLI args

[0.1.0]: https://github.com/Jinteg/JRandomly/releases/tag/v0.1.0

