# ADR 0002: JRandomly – parallel-safe determinism via scoped seeds

**Status:** Accepted  
**Date:** 2026-02-06
**Updated:** 2026-02-15 — added auto-generated root seed (Section 5)

## Context

JRandomly must be a reliable tool for test suites that commonly run with parallel execution (JUnit parallel, build tools, CI runners).

We already decided in ADR-0001 that:
- a root seed can be injected externally (system properties > environment),
- `JRandomly.randomly()` returns a new instance each call,
- each instance uses a single RNG source,
- and instance seeds must be derived deterministically from the root seed to avoid identical sequences.

However, parallel execution makes *call-order-based* derivation strategies (e.g., "instance #1, #2, #3") non-deterministic across runs because:
- thread scheduling can change the order in which instances are created,
- tests may start in different orders between runs,
- and therefore the mapping “root seed → instance seed sequence” becomes unstable.

We need a deterministic approach that remains stable under concurrency and does not depend on instance creation order.

## Decision

### 1) Introduce `JRandomly.scoped(String scope)` in core

We add a core API entry point:

- `JRandomly.scoped(String scope)`

It creates a new `JRandomly` instance whose seed is derived deterministically from:
- the externally injected root seed (if present), and
- the provided `scope` identifier.

**Rationale:** A stable, user-controlled scope (e.g., a test id like `MyTest#shouldCreateOrder`) yields a stable sequence regardless of parallel scheduling.

### 2) Deterministic seed derivation is scope-based (not call-order-based)

When a root seed is provided, the instance seed is derived as:

- `instanceSeed = mix64(rootSeed XOR hash64(scope))`

Where:
- `hash64(scope)` is a deterministic 64-bit hash of the `scope` string (UTF-8 bytes).
- `mix64(x)` is a well-defined 64-bit mixing function (avalanche) to distribute bits well.

**Scope rules:**
- `scope` must be non-null and non-blank.
- `scope` is case-sensitive.
- `scope` is hashed using UTF-8 encoding without locale-sensitive transformations.

**Rationale:** This makes the result independent from thread scheduling and call order while keeping the API test-framework-agnostic.

### 3) Behavior when no root seed is provided

- If no root seed is provided externally, `scoped(scope)` still returns a valid instance.
- In this case, the seed is derived from an internal entropy source (implementation-defined) and the scope may be used as additional salt, but **cross-run reproducibility is not guaranteed** without a root seed.

**Rationale:** Reproducibility requires a stable external root seed; scope alone cannot guarantee identical runs if the root seed is absent.

### 4) Relationship to `JRandomly.randomly()`

- `JRandomly.randomly()` remains available as a convenience API returning a new instance with default config.
- Under a root seed, `randomly()` may use a derived instance seed, but **it is not guaranteed to be stable across parallel runs** if the derivation depends on instance creation order.
- Therefore, for parallel-stable determinism, `scoped(scope)` is the recommended API.

**Rationale:** Keep the "just works" experience, while providing a robust parallel-safe option.

### 5) Auto-generated root seed when none is provided externally

When no root seed is provided via system property or environment variable, `JRandomly` **generates a random root seed once per JVM lifetime** and caches it in a static holder.

- The auto-generated seed is computed from runtime entropy (`System.nanoTime()`), mixed via `SeedDerivation.seedFromEntropy(...)`.
- All subsequent calls to `randomly()` and `scoped(scope)` derive their instance seeds from this cached root seed — exactly the same way as if the seed had been provided externally.
- The auto-generated root seed appears in `replayInfo()` and in the replay file, so **every run is reproducible after the fact**: users copy the logged seed and pass it as `-Djrandomly.seed=...` to replay the entire run.

**Consequences:**

- `rootSeed` in `JRandomlyConfig` remains an `Optional<Long>` (reflecting what the user *provided*).
- `JRandomly` resolves this to an *effective* root seed: either the external value or the auto-generated one.
- `replayInfo()` always outputs the effective root seed — no more branching between root seed and instance seed.
- `ConfigLoader` stays unchanged: it reads external configuration only and does not manage seed lifecycle.

**Rationale:** This eliminates the split behavior between "seeded run" and "unseeded run." Every run has a root seed; the only difference is whether the user provided it or JRandomly generated it. Replay is always possible.

### 6) Named forks within an instance (added 2026-10-05, version 0.4.0)

Scopes make instances independent of each other, but all values of one instance still come
from a single stream: inserting a call shifts every following value, which breaks tests that
rely on specific values.

`JRandomly.fork(String name)` returns a new instance whose seed is derived from the parent's
instance seed and the name:

```
forkSeed = seedForSubstream(instanceSeed, "fork:" + name)
```

- Forking does not consume values from the parent.
- The same name always yields the same stream; forks can be nested.
- Configuration (locale, `runStartTime`, `maybeRate`) is inherited.
- Forks are separate instances and can be handed to different threads, which makes
  multithreaded test code reproducible (one fork per thread).
- Forks do not write replay entries; they are reproducible from the parent's scope and name.
- The derivation (`"fork:"` prefix, `seedForSubstream`) is part of the reproducibility contract
  (ADR-0008) and must stay stable across versions.

## Consequences

### Positive
- Deterministic, parallel-safe sequences are achievable:
    - same root seed + same scope ⇒ same sequence across runs,
    - independent of thread scheduling and test execution order.
- No test framework lock-in: any caller can construct a stable scope id.
- Enables higher confidence in reproducing failing tests in CI.

### Trade-offs
- Callers must provide stable and unique scopes to avoid accidental collisions.
- Two different scopes could theoretically collide at the hash level (extremely unlikely with 64-bit hashing; acceptable for test data).
- Without a root seed, reproducibility across runs is not guaranteed (by design).

## Testing strategy

Add tests that verify:

1) **Scope determinism**
- Given a fixed root seed and a fixed scope, `scoped(scope)` always produces the same first N values across runs.

2) **Parallel stability**
- In a multi-threaded test, multiple threads calling `scoped("A")` produce identical sequences for "A" regardless of interleavings.
- Different scopes ("A" vs "B") produce different sequences (high probability).

3) **Non-dependence on call order**
- Creating instances in different orders (A then B vs B then A) yields identical results per scope.

## Next steps

- Finalize and document the exact `hash64` and `mix64` functions in the implementation (must be stable across versions).
- Implement `JRandomly.scoped(String scope)` and ensure it uses the same config resolution as `randomly()`.
- Update README with guidance:
    - use external root seed + `scoped(scope)` for parallel-safe determinism.