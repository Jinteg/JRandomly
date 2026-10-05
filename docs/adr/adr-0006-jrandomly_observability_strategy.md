# ADR 0006: JRandomly – observability strategy (replay file, logging, warnings)

**Status:** Accepted  
**Date:** 2026-02-15

## Context

JRandomly must allow users to reproduce failing test runs. This requires that the runtime configuration (seed, runStartTime, locale) is **observable** without being intrusive.

Early implementations used `System.Logger` at `INFO` level, which caused excessive console noise (~127 log lines for a moderate test suite). This is unacceptable for a library that should "stay out of the way."

## Decision

### 1) Default mode: silent (no console output)

JRandomly does **not** write to the console by default.  
Internal logging uses `System.Logger` at `DEBUG` level (visible only when explicitly configured).

### 2) Replay file: `target/jrandomly-replay.txt`

On each `JRandomly` instance creation, a single line is **appended** to `target/jrandomly-replay.txt`:
2026-02-15T09:26:47Z | scoped("MyTest#x") | -Djrandomly.seed=123456 -Djrandomly.runStartTime=2026-02-07T12:30:56Z -Djrandomly.locale=de-DE | instanceSeed=4064615914871728083

This file:

- is created lazily (only when the first instance is created),
- is truncated at the start of each JVM run (not appended across runs),
- lives in `target/` so `mvn clean` removes it,
- contains one line per instance (compact, greppable).

**Update 2026-10-05 (version 0.4.0):** The location is configurable via
`jrandomly.replayFile` / `JRANDOMLY_REPLAY_FILE`, resolved once per JVM:

- not set → `target/jrandomly-replay.txt` (unchanged default),
- `off` (also `false`, `none`) → no replay file and no file access at all,
- any other value → used as file path, e.g. `build/jrandomly-replay.txt` for Gradle builds.

Entries are appended under a lock, so lines of parallel tests never interleave. File errors
never fail a test run: for an explicitly configured path a single `WARNING` is logged, for
the default location only `DEBUG`. Forks (`fork(name)`) do not write entries; they are
reproducible from their parent.

### 3) Suppress replay file when seed is externally set

When `jrandomly.seed` is explicitly provided (via system property or env), the replay file is **still written** but a header line indicates that the run is already seeded:

Root seed provided: 123456 — this run is reproducible

This avoids confusion ("do I need to save these values?").

### 4) `replayInfo()` API remains available

Users can always call `r.replayInfo()` programmatically to get the CLI-friendly replay string. This is useful in:

- custom failure handlers,
- JUnit extensions (future),
- manual debugging.

### 5) Warnings

JRandomly may emit `WARNING`-level log messages for:

- invalid or suspicious configuration (e.g., `maybeRate` very close to 1.0),
- catalog loading failures (before throwing exceptions).

These are intentionally rare and always indicate something the user should address.

## Consequences

### Positive

- Console stays clean by default.
- Replay info is always available in `target/jrandomly-replay.txt`.
- No dependency on external logging frameworks.

### Trade-offs

- File I/O on every instance creation (minimal cost; append-only, no flush per line).
- Users must know to look in `target/` for the replay file (documented in README).

## Next steps

- Implement `ReplayFileWriter` (internal utility).
- Document replay file location in README.
- Add integration test verifying file creation and content.