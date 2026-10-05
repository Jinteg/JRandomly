package de.jinteg.randomly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JRandomlyForkTest {

  private static final long SEED = 4711L;

  private static final Instant RUN_START_TIME = Instant.parse("2026-01-01T00:00:00Z");

  private static JRandomly fixed(String scope) {
    return JRandomly.builder()
        .withSeed(SEED)
        .withScope(scope)
        .withRunStartTime(RUN_START_TIME)
        .withLocale(Locale.GERMANY)
        .withMaybeRate(0.3)
        .build();
  }

  private static List<Integer> values(JRandomly r) {
    return IntStream.range(0, 10).mapToObj(i -> r.intBetween(0, 1_000_000)).toList();
  }

  @Test
  @DisplayName("Fork values do not depend on calls made on the parent before")
  void fork_is_independent_of_parent_consumption() {
    JRandomly untouched = fixed("ForkTest#independent");
    JRandomly used = fixed("ForkTest#independent");
    used.bool();
    used.text().alpha(20);
    used.person().data();

    assertThat(values(used.fork("customer")))
        .isEqualTo(values(untouched.fork("customer")));
  }

  @Test
  @DisplayName("Forking does not consume values of the parent")
  void fork_does_not_consume_parent_values() {
    JRandomly withFork = fixed("ForkTest#noConsumption");
    JRandomly withoutFork = fixed("ForkTest#noConsumption");

    values(withFork.fork("a"));
    values(withFork.fork("b").fork("c"));

    assertThat(values(withFork)).isEqualTo(values(withoutFork));
  }

  @Test
  @DisplayName("Same name yields the same stream, different names yield different streams")
  void same_name_same_stream_different_name_different_stream() {
    JRandomly r = fixed("ForkTest#names");

    List<Integer> first = values(r.fork("customer"));
    List<Integer> second = values(r.fork("customer"));
    List<Integer> other = values(r.fork("supplier"));

    assertThat(second).isEqualTo(first);
    assertThat(other).isNotEqualTo(first);
  }

  @Test
  @DisplayName("Forks of different scopes differ")
  void forks_of_different_scopes_differ() {
    assertThat(values(fixed("ForkTest#scopeA").fork("customer")))
        .isNotEqualTo(values(fixed("ForkTest#scopeB").fork("customer")));
  }

  @Test
  @DisplayName("Nested forks are deterministic and differ from a flat fork")
  void nested_forks_are_deterministic() {
    List<Integer> nested = values(fixed("ForkTest#nested").fork("order").fork("items"));
    List<Integer> nestedAgain = values(fixed("ForkTest#nested").fork("order").fork("items"));
    List<Integer> flat = values(fixed("ForkTest#nested").fork("items"));

    assertThat(nestedAgain).isEqualTo(nested);
    assertThat(flat).isNotEqualTo(nested);
  }

  @Test
  @DisplayName("Fork inherits locale, runStartTime and replay info")
  void fork_inherits_configuration() {
    JRandomly parent = fixed("ForkTest#config");
    JRandomly fork = parent.fork("customer");

    assertThat(fork.getLocale()).isEqualTo(parent.getLocale());
    assertThat(fork.getRunStartTime()).isEqualTo(parent.getRunStartTime());
    assertThat(fork.replayInfo()).isEqualTo(parent.replayInfo());
    assertThat(fork.getInstanceSeed()).isNotEqualTo(parent.getInstanceSeed());
    assertThat(fork.getScopeLabel())
        .isEqualTo("scoped(\"ForkTest#config\")/fork(\"customer\")");
    assertThat(fork.fork("items").getScopeLabel())
        .isEqualTo("scoped(\"ForkTest#config\")/fork(\"customer\")/fork(\"items\")");
  }

  @Test
  @DisplayName("Forks handed to threads stay reproducible")
  void forks_in_threads_are_reproducible() throws Exception {
    assertThat(valuesPerWorker()).isEqualTo(valuesPerWorker());
  }

  private static List<List<Integer>> valuesPerWorker() throws Exception {
    JRandomly r = fixed("ForkTest#threads");
    try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
      List<Future<List<Integer>>> futures = new ArrayList<>();
      for (int i = 0; i < 4; i++) {
        JRandomly worker = r.fork("worker-" + i);
        futures.add(executor.submit(() -> values(worker)));
      }
      List<List<Integer>> result = new ArrayList<>();
      for (Future<List<Integer>> future : futures) {
        result.add(future.get());
      }
      return result;
    }
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   "})
  @DisplayName("Fork rejects a null or blank name")
  void fork_rejects_blank_name(String name) {
    JRandomly r = fixed("ForkTest#invalid");

    assertThatThrownBy(() -> r.fork(name))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("fork name");
  }
}
