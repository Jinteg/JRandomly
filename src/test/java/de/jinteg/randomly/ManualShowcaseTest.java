package de.jinteg.randomly;

import de.jinteg.randomly.maybe.Maybe;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Showcase of various features of JRandomly.
 * This is not a real test, but rather a playground for interactive exploration of the API and its features.
 * <p>
 * You can run this test repeatedly to explore the API. Recognize how it behaves if you change the seed or locale and runStart Time
 */
//@Disabled("Manual showcase – enable locally for interactive exploration")
class ManualShowcaseTest {

  @AfterEach
  void cleanup() {
    System.clearProperty("jrandomly.locale");
  }

  @Test
  @DisplayName("Showcase - Default without system Locale ")
  void showcasePrimaryNotScoped(TestReporter reporter) {
    System.setProperty("jrandomly.locale", "de-DE");

    JRandomly r = JRandomly.randomly();

    assertThat(r.getLocale()).isEqualTo(Locale.GERMANY);
    String replayInfo = r.replayInfo();
    reporter.publishEntry("JRandomly replayInfo", replayInfo);
  }

  @Test
  @DisplayName("Showcase - Default with locale")
  void usesJvmDefaultLocale_whenNoPropertyIsSet() {
    Locale previousDefault = Locale.getDefault();
    try {
      System.clearProperty("jrandomly.locale");
      Locale.setDefault(Locale.GERMANY);

      JRandomly r = JRandomly.randomly();

      assertThat(r.getLocale()).isEqualTo(Locale.GERMANY);
    } finally {
      Locale.setDefault(previousDefault);
    }
  }

  @Test
  @DisplayName("Showcase - Manual test ")
  void showcasePrimaryTypes(TestReporter reporter) {
    System.setProperty("jrandomly.seed", "123456");
    System.setProperty("jrandomly.runStartTime", "2026-02-07T13:30:56+01:00");
    System.setProperty("jrandomly.locale", "de-DE");

    JRandomly r = JRandomly.randomly("Showcase#1");
    assertThat(r.getLocale()).isEqualTo(Locale.GERMANY);
    String replayInfo = r.replayInfo();

    reporter.publishEntry("JRandomly replayInfo", replayInfo);

    // Root seed is preserved in replayInfo (not the derived instance seed)
    assertThat(replayInfo).contains("-Djrandomly.seed=123456")
        // runStartTime is always normalized to UTC (Instant.toString())
        .contains("-Djrandomly.runStartTime=2026-02-07T12:30:56Z")
        .contains("-Djrandomly.locale=de-DE");

    reporter.publishEntry("--- Testing primary types ---");
    reporter.publishEntry("locale", r.getLocale().toString());

    // Booleans are supported with various methods and parameters:
    reporter.publishEntry("boolean=" + r.bool());

    // Integers are supported with various methods and parameters:
    reporter.publishEntry("int(0-1000)=" + r.intBetween(0, 1000));

    // Strings are supported with various methods and parameters:
    reporter.publishEntry("alphaNumeric(12)=" + r.text().alphaNumeric(12));
    reporter.publishEntry("numericString(10)=" + r.text().numericString(10));
    reporter.publishEntry("hexString(16)=" + r.text().hexString(16));
    reporter.publishEntry("prefixedAlphaNumeric(ABC, 8..12)=" + r.text().prefixedAlphaNumeric("ABC", 8, 12));

    reporter.publishEntry("finance.stockSymbol(DE)=" + r.finance().stockSymbol());
    reporter.publishEntry("finance.stockSymbol(US)=" + r.finance().stockSymbol(Locale.US));

    // Enums and Collections are supported as well, but not all methods aren't implemented yet.
    reporter.publishEntry("enumValue(CarBrand)=" + r.enumOf(CarBrand.class));

    List<String> coll = new ArrayList<>();
    coll.add("A");
    coll.add("B");
    coll.add("C");
    reporter.publishEntry("element(A,B,C)=" + r.elementOf(coll));
  }

  @Test
  @DisplayName("Showcase - Manual test with Maybe Strings ")
  void showCaseMaybeString(TestReporter reporter) {
    JRandomly r = JRandomly.randomly("Showcase#2");

    String replayInfo = r.replayInfo();
    reporter.publishEntry("replayInfo", replayInfo);
    assertThat(replayInfo).contains("-Djrandomly.seed=")
        .contains("-Djrandomly.runStartTime=")
        .contains("-Djrandomly.locale=");

    reporter.publishEntry("--- Testing Maybe String Cases ---");

    String orEmpty = r.maybe().text(() -> r.text().prefixedAlphaNumeric("X", 0, 10)).orEmpty();
    reporter.publishEntry("maybe().text(...).orEmpty()=" + orEmpty);
    assertThat(orEmpty.isEmpty() || orEmpty.startsWith("X")).isTrue();

    // String-Creation with maybe() and supplier
    String orNull = r.maybe().text(() -> r.text().prefixedAlphaNumeric("X", 0, 10)).orNull();
    reporter.publishEntry("orEmpty(x)=" + orNull);

    Optional<String> optionalText = r.maybe().value(() -> r.text().prefixedAlphaNumeric("X", 0, 10)).optional();
    reporter.publishEntry("Optional<String>(x)=" + optionalText);

    String orElse = r.maybe().value(() -> r.text().prefixedAlphaNumeric("X", 0, 10)).orElse("Cool By Default");
    reporter.publishEntry("Optional<String>(x)=" + orElse);

    String orNull2 = r.maybe().text(r.text().prefixedAlphaNumeric("X", 0, 10)).orNull();
    reporter.publishEntry("orEmpty(x)=" + orNull2);
    Optional<String> optionalText2 = r.maybe().value(r.text().prefixedAlphaNumeric("X", 0, 10)).optional();
    reporter.publishEntry("Optional<String>(x)=" + optionalText2);
    String orElse2 = r.maybe().value(r.text().prefixedAlphaNumeric("X", 0, 10)).orElse("Cool By Default");
    reporter.publishEntry("Optional<String>(x)=" + orElse2);
  }

  @Test
  @DisplayName("Showcase - Manual test with Maybe Strings ")
  void showCaseWithMaybeStringInFluentStyle(TestReporter reporter) {
    JRandomly r = JRandomly.randomly("Showcase#2");

    reporter.publishEntry("--- Testing Maybe String Cases ---");

    String orEmpty = r.maybeText(r.text().prefixedAlphaNumeric("X", 0, 10)).orEmpty();
    reporter.publishEntry("maybeText(...).orEmpty()=" + orEmpty);
    Assertions.assertThat(orEmpty.isEmpty() || orEmpty.startsWith("X")).isTrue();

    String orNull = r.maybeText(r.text().prefixedAlphaNumeric("X", 0, 10)).orNull();
    reporter.publishEntry("orNull(x)=" + orNull);

    Optional<String> optionalText = r.maybeOf(r.text().prefixedAlphaNumeric("X", 0, 10)).optional();
    reporter.publishEntry("Optional<String>(x)=" + optionalText);

    String orElse = r.maybeOf(r.text().prefixedAlphaNumeric("X", 0, 10)).orElse("Cool By Default");
    reporter.publishEntry("orElse(x)=" + orElse);

    Maybe<String> maybeOfNullable = r.maybeOfNullable(r.text().prefixedAlphaNumeric("X", 0, 10));
    reporter.publishEntry("maybeOfNullable(x)=" + maybeOfNullable);
  }

  @Test
  @DisplayName("Showcase - Manual test with Maybe Strings ")
  void showCaseWithMaybeInFluentStyle(TestReporter reporter) {
    JRandomly r = JRandomly.randomly("Showcase#2");
    Assertions.assertThat(r.replayInfo()).contains("-Djrandomly.seed=");
    reporter.publishEntry("--- Testing Maybe String Cases ---");

    Optional<CarBrand> optional = r.maybeOf(r.enumOf(CarBrand.class)).optional();
    reporter.publishEntry("Optional<String>(x)=" + optional);

    CarBrand orNull1 = r.maybeOf(r.enumOf(CarBrand.class)).orNull();
    reporter.publishEntry("Optional<String>(x)=" + orNull1);

    CarBrand orElse = r.maybeOf(r.enumOf(CarBrand.class)).orElse(CarBrand.BMW);
    reporter.publishEntry("orElse(x)=" + orElse);
  }

  @Test
  @DisplayName("Showcase - Replay: run with externally set seed + runStartTime (deterministic)")
  void showcaseReplay_withExternalSeed(TestReporter reporter) {
    // Simulate: user copied replayInfo from a previous failing run
    System.setProperty("jrandomly.seed", "123456");
    System.setProperty("jrandomly.runStartTime", "2026-02-07T12:30:56Z");
    System.setProperty("jrandomly.locale", "de-DE");

    JRandomly r = JRandomly.randomly("Showcase#replay");
    Assertions.assertThat(r.replayInfo()).contains("-Djrandomly.seed=");

    reporter.publishEntry("=== REPLAY MODE (seed externally set) ===");
    reporter.publishEntry("replayInfo=" + r.replayInfo());
    reporter.publishEntry("int(0-100)=" + r.intBetween(0, 100));
    reporter.publishEntry("alphaNumeric(8)=" + r.text().alphaNumeric(8));
    reporter.publishEntry("localDate()=" + r.dateTime().localDate());
    reporter.publishEntry("localDateBefore(30)=" + r.dateTime().localDateBefore(30));
    reporter.publishEntry("localTime()=" + r.dateTime().localTime());
    reporter.publishEntry("stockSymbol(DE)=" + r.finance().stockSymbol());

    // Run this test twice: the output must be IDENTICAL because seed + runStartTime are fixed.
  }

  @Test
  @DisplayName("Showcase - Fresh run: no seed set (non-deterministic, observe replayInfo)")
  void showcaseFreshRun_withoutSeed(TestReporter reporter) {
    // No seed set → each run produces different values,
    // But replayInfo tells you what to set for reproduction!
    System.clearProperty("jrandomly.seed");
    System.clearProperty("jrandomly.runStartTime");
    System.clearProperty("jrandomly.locale");

    JRandomly r = JRandomly.randomly("Showcase#fresh");

    reporter.publishEntry("=== FRESH RUN (no seed set) ===");
    reporter.publishEntry("replayInfo", r.replayInfo());
    Assertions.assertThat(r.replayInfo()).contains("-Djrandomly.seed=");
    reporter.publishEntry("  ^^^ Copy the -D flags above to reproduce this run!");
    reporter.publishEntry("int(0-100)=" + r.intBetween(0, 100));
    reporter.publishEntry("alphaNumeric(8)", r.text().alphaNumeric(8));
    reporter.publishEntry("localDate()=" + r.dateTime().localDate());
    reporter.publishEntry("localTime()=" + r.dateTime().localTime());
    reporter.publishEntry("stockSymbol(DE)", r.finance().stockSymbol());

    // Run this test twice: the output will DIFFER.
    // But if you take the replayInfo and set it as -D flags, it becomes deterministic.
  }

  private enum CarBrand {
    BMW, AUDI, MERCEDES, PORSCHE, TESLA, HONDA, TOYOTA, FORD
  }
}