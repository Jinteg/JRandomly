# ADR-0008: Scope of the reproducibility contract

## Status

Accepted

## Context

Reproducibility is JRandomly's core promise: a failing test run can be replayed with the
logged seed, `runStartTime` and locale (see ADR-0001, ADR-0002, ADR-0004).

The existing ADRs state that the same root seed and scope produce the same sequence
"across runs". They do not state whether this also holds **across library versions**.

Two fixes made the question concrete:

- Numbered catalogs with several key groups (e.g. `xde40.n`, `xdem50.n`, `xdes60.n` in the
  stock catalogs) were collected by entry number only. Entries with the same number
  overwrote each other, so only 60 of 150 German and 101 of 151 English stocks were
  reachable, and the surviving group per position depended on the hash-based iteration
  order of `java.util.Properties`. Fixing this changes the values `finance().stock()`
  returns for an existing seed.
- `GenderRegistry` used `Map.copyOf`, whose iteration order is randomized per JVM run.
  `person().gender()` therefore returned different values for the same seed **within the
  same version**, i.e. the contract was broken without anyone noticing.

Any change to catalog data changes generated values as well: adding an entry changes the
catalog size and therefore the selected index; correcting a typo changes the returned text.
A library with growing catalogs cannot keep values identical across all versions.

## Decision

### 1. What is guaranteed

Within the **same JRandomly version**, the same

- root seed,
- scope (`randomly("scope")` / `builder().withScope(...)`),
- `runStartTime`,
- locale,
- `maybeRate`,
- and the same sequence of calls on the instance

produce the **same values on every run, on every JVM and operating system**.

### 2. What is not guaranteed

Values for an existing seed may change between versions when

- catalog data changes (entries added, removed, reordered or corrected),
- selection or generation logic changes,
- the RNG algorithm or seed derivation changes (the latter must stay stable, see ADR-0002).

### 3. How changes are handled

- Every change that alters values for an existing seed is listed in the CHANGELOG under a
  dedicated **"Reproducibility"** heading of the release.
- Before 1.0.0, such changes are allowed in minor releases. From 1.0.0 on, changes to
  selection logic or the RNG require a minor release at minimum; catalog data corrections
  are allowed in any release but must be listed.
- `ReproducibilityContractTest` pins the output of every module for fixed inputs. A failure
  signals a change of generated values. The expected values may only be updated together
  with a CHANGELOG entry.

### 4. Implementation rules

Generators must not depend on unspecified iteration orders or JVM-specific state:

- no `Map.of`, `Set.of`, `Map.copyOf`, `Set.copyOf`, `HashMap` or `HashSet` iteration where
  the order influences selection – use `List`, `LinkedHashMap` or sorted collections,
- catalog order is defined by key prefix, then entry number (`NumberedPropertiesCatalog`),
- no wall-clock time (`Instant.now()`, `LocalDate.now()`) – use `runStartTime`,
- no `Math.random()`, `new Random()` or other RNGs – use the instance RNG,
- seed derivation for scopes, unscoped instances and forks (`SeedDerivation`, `"fork:"`
  prefix) must not change (ADR-0002).

### 5. Caller responsibilities

The guarantee also depends on inputs that only the caller controls:

- Use **scoped** instances for parallel tests. Unscoped `randomly()` instances depend on
  creation order (ADR-0002).
- Pass collections with a stable order (`List`, `LinkedHashSet`, `TreeSet`, enums) to
  `elementOf` / `elementsOf`. A `HashSet` of objects with identity-based hash codes has no
  stable order.
- `dateTime()` uses the system default time zone. Use `dateTime(ZoneId)` for values that
  must not depend on the machine.
- All values of one instance come from one stream, so the **sequence of calls** is part of the
  input. Use `fork(name)` for groups of values that must stay stable when other calls are
  added, and one fork per thread for multithreaded test code.

## Known gaps

- Values rendered by the JDK from locale data, such as `finance().currencySymbol()`
  (`Currency.getSymbol(locale)`), come from the JDK's CLDR data. The **selection** is
  reproducible, but the rendered symbol may differ between JDK versions if CLDR changes.

### Resolved

- `finance().currency*()` selected from `Currency.getAvailableCurrencies()`, whose order
  and content depend on the JDK. It now uses a fixed, ordered list of 36 ISO 4217
  currencies. `currencySymbol()` now renders symbols for the configured locale instead of
  the JVM default locale.

## Consequences

### Positive

- The promise is precise and testable instead of implicit.
- Catalogs can grow and be corrected without breaking an unwritten rule.
- Unintended changes of generated values are caught by a test, not by users.

### Negative

- Users who pin expected values for a seed may need to update them after an upgrade.
- Every value-changing change needs a CHANGELOG entry and updated contract test values.

## Alternatives considered

### Guarantee identical values across all versions

Rejected. It would freeze every catalog forever: no new entries, no corrections, no fixes
of selection bugs such as the multi-group catalog issue.

### Guarantee identical values as long as catalogs keep their number of entries

Rejected as a contract condition, because the entry count is not sufficient and also
not necessary in the right way:

- Values change with the **same** count when an entry is corrected or entries are
  reordered (e.g. `Münchener Rüeck SE` → `Münchener Rück SE`).
- The count says nothing about generator logic or RNG changes.
- Equal counts **across languages** are not required: each locale uses its own catalog,
  and entries with the same number in different languages are not related. Aligning them
  would be a separate feature ("locale parity"), not a reproducibility requirement.
- Users cannot check entry counts, but they can check the library version.

The count is still a useful internal signal; `NumberedPropertiesCatalogTest` checks that
every numbered entry of a catalog file is loaded.

### Keep the old stock selection for compatibility

Rejected. `stock()` selects by index from the catalog list; identical values are only
possible if the list stays at the old 60/101 entries, which means keeping the data loss.
`stockBySymbol` and `stock()` would also work on different data.

### Versioned catalogs selectable by the caller

Deferred. A setting such as `jrandomly.catalogVersion` could keep old catalog snapshots
reproducible across upgrades, at the cost of shipping multiple catalog versions.
Reconsider if users ask for cross-version replay.

### Version in replay information

The JRandomly version is provided by `JRandomlyVersion` (filled in by Maven resource
filtering), shown in the replay file header and appended to `replayInfo()` as
`-Djrandomly.version=...`. If `jrandomly.version` /
`JRANDOMLY_VERSION` is set and differs from the running version, a warning is logged once
per JVM, because generated values may differ.

## Follow-ups

- None at the time of writing. New gaps are added under "Known gaps".
