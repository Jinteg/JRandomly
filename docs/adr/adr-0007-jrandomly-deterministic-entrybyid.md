# ADR-0007: Support deterministic catalog entry selection by id

## Status

Accepted

## Context

JRandomly provides multiple domain APIs backed by numbered catalog property files,
for example for finance, person, and future company data.

Today, catalog-backed APIs primarily expose random selection methods, such as:

- `randomly.finance().stock()`
- `randomly.finance().cryptoAsset()`
- `randomly.person().data()`

These methods are convenient for generated example data, but some use cases require
direct access to a specific catalog entry:

- deterministic test setup without relying on seed internals
- stable examples for documentation and demos
- targeted business scenarios using a known catalog entry
- reproducing issues with a specific dataset

The catalog files already use numbered entries, so the data model naturally supports
direct lookup by entry id.

## Decision

For catalog-backed domain APIs, we support two access patterns:

1. **Random selection**
    - Existing parameterless methods continue to return a random entry from the
      locale-specific catalog.
2. **Deterministic selection by id**
    - Additional methods with the suffix `ById(int entryId)` return the exact
      numbered catalog entry from the locale-specific catalog.

Examples:

- `randomly.finance().stock()`
- `randomly.finance().stockById(5)`
- `randomly.finance().cryptoAsset()`
- `randomly.finance().cryptoAssetById(3)`
- `randomly.person().data()`
- `randomly.person().personById(7)`

The following rules apply:

- `entryId` is **1-based**
- `entryId` maps directly to the numbered catalog entry
- `ById(...)` methods are **not random** and must not depend on RNG state
- the active locale still determines which catalog is used
- invalid ids must fail fast with `IllegalArgumentException`

Example error message:

`Catalog entry id 999 not found for stocks and locale de`

## Consequences

### Positive

- API consumers can explicitly request stable catalog entries
- test setup becomes easier and more readable
- examples and demos can use fixed domain data without relying on seed behavior
- the convention scales across catalog-backed domains
- the distinction between random generation and deterministic lookup is explicit

### Negative

- numbered catalog entries become a more visible part of the public API behavior
- changing catalog numbering may affect consumers that rely on specific ids
- each domain API needs an additional lookup method and validation logic

## Naming

The deterministic lookup method naming convention is:

`<entity>ById(int entryId)`

Examples:

- `stockById(int entryId)`
- `cryptoAssetById(int entryId)`
- `personById(int entryId)`
- `companyById(int entryId)`

For the person domain, the existing random method remains `data()` for readability
and backward compatibility. The deterministic lookup method is added as
`personById(int entryId)` instead of renaming the existing random method to avoid
an awkward API shape such as `randomly.person().person()`.

## Implementation notes

Implementation should reuse the same locale-specific catalog sources as the random
methods.

`ById(...)` methods should:

- validate that `entryId > 0`
- resolve the locale-specific catalog
- load the numbered entry with the exact matching id
- throw `IllegalArgumentException` if no such entry exists
- parse the entry using the same parser as the random method

The catalog integrity test remains responsible for validating numbered catalog
structure, uniqueness, and gap-free sequences.

## Alternatives considered

### Use overloaded methods such as `stock(int entryId)`

Rejected because the method name is too implicit. It is less clear whether the
integer represents an id, an index, a count, or another parameter.

### Use domain-neutral names such as `dataById(int entryId)`

Rejected because entity-specific names are clearer in public APIs and scale better
across multiple domains.

### Keep only random access and rely on seed determinism

Rejected because seeds are useful for reproducibility of random streams, but they
are less explicit and less convenient than direct catalog entry lookup for targeted
use cases.