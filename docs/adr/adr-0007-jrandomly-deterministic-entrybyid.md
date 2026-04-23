# ADR-0007: Support deterministic lookup for catalog-backed domain APIs

## Status

Accepted

## Context

JRandomly provides multiple domain APIs backed by catalog property files,
for example for finance, person, and future company data.

Today, catalog-backed APIs primarily expose random selection methods, such as:

- `randomly.finance().stock()`
- `randomly.finance().cryptoAsset()`
- `randomly.person().data()`

These methods are convenient for generated example data, but some use cases require
direct access to a specific catalog-backed entry:

- deterministic test setup without relying on seed internals
- stable examples for documentation and demos
- targeted business scenarios using a known dataset
- reproducing issues with a specific entry

Some catalogs are naturally addressed by numbered entries, while others are better
addressed by stable domain keys such as symbol, code, username, or VAT id.

A single mandatory lookup style based only on numbered ids would not fit all domains
equally well.

## Decision

For catalog-backed domain APIs, we support two access patterns:

1. **Random selection**
    - Existing parameterless methods continue to return a random entry from the
      locale-specific catalog.
2. **Deterministic lookup**
    - Additional explicit lookup methods return a specific catalog-backed entry by
      a stable selector appropriate for the domain.

Examples:

- `randomly.finance().stock()`
- `randomly.finance().stockBySymbol("AAPL")`
- `randomly.finance().cryptoAsset()`
- `randomly.finance().cryptoAssetBySymbol("BTC")`
- `randomly.person().data()`
- `randomly.person().personById(7)`

The following rules apply:

- random methods remain parameterless
- deterministic lookup methods must use an explicit selector in the method name
- the selector must be stable and appropriate for the domain
- lookup methods are **not random** and must not depend on RNG state
- the active locale still determines which catalog is used where applicable
- invalid selectors must fail fast with `IllegalArgumentException`

Example error messages:

- `Catalog entry id 999 not found for persons and locale de`
- `Stock symbol AAPL not found for locale de`
- `Crypto asset symbol BTC not found`

## Consequences

### Positive

- API consumers can explicitly request stable catalog-backed entries
- test setup becomes easier and more readable
- examples and demos can use fixed domain data without relying on seed behavior
- the convention scales across different domains with different natural identifiers
- the distinction between random generation and deterministic lookup is explicit

### Negative

- public APIs become slightly more domain-specific
- each domain API needs selector-specific lookup logic and validation
- selector stability must be maintained in catalog data

## Naming

Deterministic lookup methods must use explicit selector-based names.

Examples:

- `stockBySymbol(String symbol)`
- `cryptoAssetBySymbol(String symbol)`
- `personById(int entryId)`
- `companyByVatId(String vatId)`
- `genderByCode(String code)`

The selector should reflect the most natural stable key of the domain.

For the person domain, the existing random method remains `data()` for readability
and backward compatibility. The deterministic lookup method is added as
`personById(int entryId)` instead of renaming the existing random method to avoid
an awkward API shape such as `randomly.person().person()`.

## Implementation notes

Implementation should reuse the same catalog sources as the random methods.

Deterministic lookup methods should:

- validate the selector input
- resolve the locale-specific catalog where applicable
- locate the matching entry by the configured selector
- throw `IllegalArgumentException` if no matching entry exists
- parse the entry using the same parser as the corresponding random method

The catalog integrity test remains responsible for validating numbered catalog
structure, uniqueness, and gap-free sequences where numbering is used.

## Alternatives considered

### Use overloaded methods such as `stock(int entryId)`

Rejected because the method name is too implicit. It is less clear whether the
integer represents an id, an index, a count, or another parameter.

### Require `ById(int entryId)` for all catalog-backed domains

Rejected because not all domains have a natural, stable, global numeric identifier.
Some domains are better addressed by stable business keys such as stock symbols,
crypto asset symbols, or codes.

### Keep only random access and rely on seed determinism

Rejected because seeds are useful for reproducibility of random streams, but they
are less explicit and less convenient than direct deterministic lookup for targeted
use cases.