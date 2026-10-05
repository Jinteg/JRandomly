# JRandomly – Roadmap

> Living document. Updated: 2026-10-05.
> This roadmap captures planned features, improvements, and research topics.
> Items are grouped by phase, not by fixed timeline.

---

## ✅ Phase 0 – Foundation (done)

- [x] Core API: `JRandomly.randomly()`, `builder()`, `scoped()`
- [x] Deterministic seed derivation (`SeedDerivation`)
- [x] External config: system properties + env variables
- [x] `runStartTime` time anchor (ADR-0004)
- [x] Core modules: `text()`, `dateTime()`, `id()`
- [x] Domain module: `finance()` with locale-aware catalogs
- [x] `maybe()` cross-cutting utilities
- [x] `replayInfo()` for reproducibility
- [x] Replay file writer (observability, ADR-0006)
- [x] `.properties`-based catalogs (`NumberedPropertiesCatalog`)

---

## Phase 1 – Stabilization & Core Completeness (current)

### Core modules

- [ ] `num()` – numeric generation module (int ranges, doubles, BigDecimal)
- [ ] `coll()` – collection helpers (list/set/map sampling, builders)
- [ ] `date()` / `time()` split from `dateTime()` if needed

### API polish

- [ ] Instance key (`instanceKey()`) – deterministic short ID per instance for traceability
- [ ] `r.text().traced("field")` – opt-in traced/decorated strings using instance key
- [ ] Review and stabilize all public API signatures

### Infrastructure

- [ ] JUnit 5 Extension – automatic `replayInfo()` output on test failure (inspired by value-provider)
- [ ] Seed info as suppressed exception on assertion failures

### Quality

- [ ] Increase test coverage for edge cases (long overflow, empty catalogs, locale fallback)
- [x] Reproducibility contract test pinning generated values (ADR-0008)
- [ ] Property-based tests for determinism guarantees
- [ ] Performance baseline (micro-benchmarks for hot paths)

---

## Phase 2 – Extensibility & Domains

### Custom domain extensions (planned ADR-0009, experimental)

- [ ] `JRandomlyDomain` marker interface
- [ ] `r.domain(Class<T>)` with per-instance caching
- [ ] Documentation + example custom domain

### Built-in domains

- [x] `r.person()` – names, birth dates, emails, gender (locale-aware: de, en, ja, tr)
- [ ] `r.address()` – streets, cities, zip codes (locale-aware)
- [x] `r.company()` – company names, addresses, contact data, VAT IDs (de, en, ja, tr)
- [ ] `r.net()` – emails, URLs, IP addresses

### Domain object model (research)

- [ ] Rich domain objects with generated IDs (e.g. `RandomPerson` with `id()`)
- [ ] Object relation tracking: `r.order().forPerson(person)` (deferred, needs design)
- [ ] Evaluate: graph of related test objects with deterministic IDs

---

## Phase 3 – Advanced Features (evaluate)

### Catalog & format

- [ ] YAML catalogs as optional module (separate artifact, no core dependency)
- [ ] Schema-based output (CSV/JSON transformer layer)
- [ ] Weighted catalog entries (probability-driven selection)

### Discovery

- [ ] ServiceLoader-based auto-discovery for domain JARs (evaluate startup cost)
- [ ] `@JRandomlyDomain` annotation with metadata (display name, description)

### Integrations

- [ ] Maven/Gradle plugin for seed propagation in CI
- [ ] IDE integration hints (e.g. live template suggestions)

---

## Explicitly out of scope

- **Broad demo data generation** (Datafaker territory) – we focus on reproducible test data
- **Inheritance-based extension** (value-provider pattern) – we use composition
- **YAML as default catalog format** – `.properties` stays as baseline
- **Automatic classpath scanning at startup** – conflicts with fast & lean goal

---

## Design principles (constant)

1. **Determinism first** – same seed + scope + runStartTime + locale = same output, always
2. **Parallel-safe by design** – `scoped()` instances are independent streams
3. **Lean core** – zero runtime dependencies, fast startup
4. **Composition over inheritance** – modules and domains, not base classes
5. **Opt-in complexity** – advanced features (tracing, relations) are never required

---

## Assessments (reference)

| Library        | Assessment date | Key takeaway for JRandomly                                      |
|----------------|-----------------|-----------------------------------------------------------------|
| JavaFaker      | 2026-02-07      | Determinism must be first-class, not best-effort                |
| Datafaker      | 2026-02-12      | Inspiration for breadth, but not for core design                |
| Value-Provider | 2026-02-18      | Replay UX + traceability inspiration; composition > inheritance |
