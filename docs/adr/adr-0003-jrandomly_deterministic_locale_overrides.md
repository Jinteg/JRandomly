# ADR 0003: JRandomly – Locale handling for domain catalogs

**Status:** Superseded (revised 2026-02-15)  
**Date:** 2026-02-07  
**Revised:** 2026-02-15

> **Implementation status (2026-10-05):** Mechanism **B** (locale parameter on the generator
> method, e.g. `finance().stock(Locale)`, `person().data(Locale)`, `company().data(Locale)`)
> is implemented. Mechanism **A** (`r.finance(Locale)` on the domain entry point) is not
> implemented yet. Decision 4 (locale does not influence the RNG stream) applies as written.

## Context

JRandomly must provide locale-aware domain catalogs (EN/DE initially), with the ability to override locale for a specific domain call.

The original version of this ADR (2026-02-07) proposed `withLocale(Locale)` on `JRandomly` as a method that creates a deterministic derived substream (new RNG instance with a locale-keyed seed). This was implemented and tested.

After introducing the Builder pattern, the design was re-evaluated:

- **Locale does not affect the RNG stream.** It only selects which catalog (`.properties` resource) is used for domain-specific data.
- Creating a full RNG substream just to change catalog selection is **over-engineered** for this use case.
- The Builder already accepts `withLocale(Locale)` to set the **default locale** for the instance — this covers the common case.
- What remains is the question: how does the user **override locale at call-time** for a specific domain invocation?

## Decision

### 1) Default locale is set via Builder (instance-level)

The `JRandomly.Builder` accepts `withLocale(Locale)` to configure the default locale for the instance. This locale is used by all domain modules unless explicitly overridden.
java JRandomly r = JRandomly.builder() .withSeed(42L) .withLocale(Locale.GERMANY) .build();
r.finance().stockSymbol(); // uses DE catalog

### 2) Locale override happens at the domain module level (not on JRandomly)

For call-time locale overrides, we provide two complementary mechanisms:

**A) Override on domain entry point (preferred)**

```java
r.finance(Locale.ITALY).stockSymbol(); // IT catalog, same RNG
```

The domain module receives the overridden locale and uses it for catalog selection. The RNG stream is **not affected** — only the catalog lookup changes.
**B) Override on individual generator method (supplementary)**

``` java
r.finance().stockSymbol(Locale.ITALY); // IT catalog for this call only
``` 

Useful when only a single method needs a different locale within an otherwise default-locale domain context.

### 3) `withLocale(Locale)` is removed from JRandomly instance API

There is no `r.withLocale(Locale)` method that returns a new instance. Locale is either: `JRandomly`

- configured at build time (Builder), or
- overridden at domain/method level.

### 4) Locale does NOT influence RNG seed derivation

The locale is purely a **catalog selector**. Two instances with the same seed and scope but different locales produce **identical RNG sequences**. Only the catalog-based domain output may differ.
This is a deliberate simplification compared to the original ADR-0003.
## Consequences
### Positive

- Simpler mental model: locale = catalog selection, seed = RNG stream. Clear separation.
- No unnecessary RNG substream creation for locale overrides.
- Consistent with ADR-0005 module structure (domains own their parameters).
- Both override levels (domain-wide and per-method) are available for flexibility.

### Trade-offs

- Domain modules must accept an optional parameter (small implementation cost). `Locale`
- If a future use case requires locale to affect the RNG stream, this decision must be revisited.

### What changed from the original ADR-0003

- **Removed:** `withLocale(Locale)` as a substream-creating method on . `JRandomly`
- **Removed:** Locale-keyed seed derivation (`seedForSubstream(parentSeed, "locale:" + tag)`).
- **Added:** Locale override at domain module level (`r.finance(Locale)`) and method level (`stockSymbol(Locale)`).
- **Clarified:** Locale is a catalog parameter, not an RNG parameter.

## Implementation notes

- already supports `stockSymbol(Locale)` (method-level override). `FinanceRandomly`
- Domain entry point override (`r.finance(Locale)`) requires adding a `finance(Locale)` method on that passes the locale to . `JRandomly``FinanceRandomly`
- The default locale from is used when no override is provided. `JRandomlyConfig`

## Testing strategy

- Same seed + scope + different locale → **identical** RNG sequences ( etc.). `intBetween`
- Same seed + scope + different locale → **different** catalog-based output (`stockSymbol`).
- `r.finance(Locale.US).stockSymbol()` uses the US catalog regardless of instance default locale.
- `r.finance().stockSymbol(Locale.US)` uses the US catalog for that call only.