package de.jinteg.randomly;

import de.jinteg.randomly.maybe.Maybe;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Manual playground for exploring JRandomly usage patterns and behavior.
 * This class is not part of the regular automated regression suite.
 * <p>
 * Enable and run locally when you want to try ideas, inspect replay info,
 * or explore implementation details interactively.
 */
@Disabled("Manual playground - enable locally for interactive exploration")
class ManualUsagePlaygroundTest {

    @Test
    @DisplayName("Manual Playground - Default configuration")
    void showcasePrimaryNotScoped() {
        System.clearProperty("jrandomly.seed");
        System.clearProperty("jrandomly.runStartTime");
        System.clearProperty("jrandomly.locale");
        System.clearProperty("jrandomly.maybeRate");


        JRandomly r = JRandomly.randomly();
        String replayInfo = r.replayInfo();
        System.out.println("JRandomly replayInfo=" + replayInfo);
        assertThat(r.getLocale()).isEqualTo(Locale.getDefault());
    }

    @Test
    @DisplayName("Manual Playground - Primary types")
    void showcasePrimaryTypes() {
        System.setProperty("jrandomly.seed", "123456");
        System.setProperty("jrandomly.runStartTime", "2026-02-07T13:30:56+01:00");
        System.setProperty("jrandomly.locale", "de-DE");

        JRandomly r = JRandomly.randomly("Showcase#1");
        assertThat(r.getLocale()).isEqualTo(Locale.GERMANY);
        String replayInfo = r.replayInfo();

        System.out.println("JRandomly replayInfo=" + replayInfo);

        // Root seed is preserved in replayInfo (not the derived instance seed)
        assertThat(replayInfo).contains("-Djrandomly.seed=123456")
                // runStartTime is always normalized to UTC (Instant.toString())
                .contains("-Djrandomly.runStartTime=2026-02-07T12:30:56Z")
                .contains("-Djrandomly.locale=de-DE");

        System.out.println("--- Testing primary types ---");
        System.out.println("locale=" + r.getLocale());

        // Booleans are supported with various methods and parameters:
        System.out.println("boolean=" + r.bool());

        // Integers are supported with various methods and parameters:
        System.out.println("int(0-1000)=" + r.intBetween(0, 1000));

        // Strings are supported with various methods and parameters:
        System.out.println("alphaNumeric(12)=" + r.text().alphaNumeric(12));
        System.out.println("numericString(10)=" + r.text().numericString(10));
        System.out.println("hexString(16)=" + r.text().hexString(16));
        System.out.println("prefixedAlphaNumeric(ABC, 8..12)=" + r.text().prefixedAlphaNumeric("ABC", 8, 12));

        System.out.println("finance.stockSymbol(DE)=" + r.finance().stockSymbol());
        System.out.println("finance.stockSymbol(US)=" + r.finance().stockSymbol(Locale.US));

        // Enums and collections are supported as well.
        System.out.println("enumValue(CarBrand)=" + r.enumOf(CarBrand.class));

        List<String> coll = new ArrayList<>();
        coll.add("A");
        coll.add("B");
        coll.add("C");
        System.out.println("element(A,B,C)=" + r.elementOf(coll));
    }

    @Test
    @DisplayName("Manual Playground - Maybe API - Strings")
    void showCaseMaybeString() {
        JRandomly r = JRandomly.randomly("Showcase#2");

        String replayInfo = r.replayInfo();
        System.out.println("replayInfo=" + replayInfo);
        assertThat(replayInfo).contains("-Djrandomly.seed=")
                .contains("-Djrandomly.runStartTime=")
                .contains("-Djrandomly.locale=");

        System.out.println("--- Testing Maybe String Cases ---");

        String orEmpty = r.maybe().text(() -> r.text().prefixedAlphaNumeric("X", 0, 10)).orEmpty();
        System.out.println("maybe().text(...).orEmpty()=" + orEmpty);
        assertThat(orEmpty.isEmpty() || orEmpty.startsWith("X")).isTrue();

        // String-Creation with maybe() and supplier
        String orNull = r.maybe().text(() -> r.text().prefixedAlphaNumeric("X", 0, 10)).orNull();
        System.out.println("orEmpty(x)=" + orNull);

        Optional<String> optionalText = r.maybe().value(() -> r.text().prefixedAlphaNumeric("X", 0, 10)).optional();
        System.out.println("Optional<String>(x)=" + optionalText);

        String orElse = r.maybe().value(() -> r.text().prefixedAlphaNumeric("X", 0, 10)).orElse("Cool By Default");
        System.out.println("Optional<String>(x)=" + orElse);

        String orNull2 = r.maybe().text(r.text().prefixedAlphaNumeric("X", 0, 10)).orNull();
        System.out.println("orEmpty(x)=" + orNull2);
        Optional<String> optionalText2 = r.maybe().value(r.text().prefixedAlphaNumeric("X", 0, 10)).optional();
        System.out.println("Optional<String>(x)=" + optionalText2);
        String orElse2 = r.maybe().value(r.text().prefixedAlphaNumeric("X", 0, 10)).orElse("Cool By Default");
        System.out.println("Optional<String>(x)=" + orElse2);
    }

    @Test
    @DisplayName("Manual Playground - Maybe API")
    void showCaseWithMaybeStringInFluentStyle() {
        JRandomly r = JRandomly.randomly("Showcase#2");

        System.out.println("--- Testing Maybe String Cases ---");

        String orEmpty = r.maybeText(r.text().prefixedAlphaNumeric("X", 0, 10)).orEmpty();
        System.out.println("maybeText(...).orEmpty()=" + orEmpty);
        Assertions.assertThat(orEmpty.isEmpty() || orEmpty.startsWith("X")).isTrue();

        String orNull = r.maybeText(r.text().prefixedAlphaNumeric("X", 0, 10)).orNull();
        System.out.println("orNull(x)=" + orNull);

        Optional<String> optionalText = r.maybeOf(r.text().prefixedAlphaNumeric("X", 0, 10)).optional();
        System.out.println("Optional<String>(x)=" + optionalText);

        String orElse = r.maybeOf(r.text().prefixedAlphaNumeric("X", 0, 10)).orElse("Cool By Default");
        System.out.println("orElse(x)=" + orElse);

        Maybe<String> maybeOfNullable = r.maybeOfNullable(r.text().prefixedAlphaNumeric("X", 0, 10));
        System.out.println("maybeOfNullable(x)=" + maybeOfNullable);
    }

    @Test
    @DisplayName("Manual Playground - Fluent maybe API - Enums")
    void showCaseWithMaybeInFluentStyle() {
        JRandomly r = JRandomly.randomly("Showcase#2");
        Assertions.assertThat(r.replayInfo()).contains("-Djrandomly.seed=");
        System.out.println("--- Testing Maybe String Cases ---");

        Optional<CarBrand> optional = r.maybeOf(r.enumOf(CarBrand.class)).optional();
        System.out.println("Optional<String>(x)=" + optional);

        CarBrand orNull1 = r.maybeOf(r.enumOf(CarBrand.class)).orNull();
        System.out.println("Optional<String>(x)=" + orNull1);

        CarBrand orElse = r.maybeOf(r.enumOf(CarBrand.class)).orElse(CarBrand.BMW);
        System.out.println("orElse(x)=" + orElse);
    }

    @Test
    @DisplayName("Manual Playground - Replay with external seed")
    void showcaseReplay_withExternalSeed() {
        // Simulate: user copied replayInfo from a previous failing run
        System.setProperty("jrandomly.seed", "123456");
        System.setProperty("jrandomly.runStartTime", "2026-02-07T12:30:56Z");
        System.setProperty("jrandomly.locale", "de-DE");

        JRandomly r = JRandomly.randomly("Showcase#replay");
        Assertions.assertThat(r.replayInfo()).contains("-Djrandomly.seed=");

        System.out.println("=== REPLAY MODE (seed externally set) ===");
        System.out.println("replayInfo=" + r.replayInfo());
        System.out.println("int(0-100)=" + r.intBetween(0, 100));
        System.out.println("alphaNumeric(8)=" + r.text().alphaNumeric(8));
        System.out.println("localDate()=" + r.dateTime().localDate());
        System.out.println("localDateBefore(30)=" + r.dateTime().localDateBefore(30));
        System.out.println("localTime()=" + r.dateTime().localTime());
        System.out.println("stockSymbol(DE)=" + r.finance().stockSymbol());

        // Run this test twice: the output must be identical because seed + runStartTime are fixed.
    }

    @Test
    @DisplayName("Manual Playground - Fresh run without external seed")
    void showcaseFreshRun_withoutSeed() {
        // No seed set -> each run produces different values,
        // but replayInfo tells you what to set for reproduction!
        System.clearProperty("jrandomly.seed");
        System.clearProperty("jrandomly.runStartTime");
        System.clearProperty("jrandomly.locale");

        JRandomly r = JRandomly.randomly("Showcase#fresh");

        System.out.println("=== FRESH RUN (no seed set) ===");
        System.out.println("replayInfo=" + r.replayInfo());
        Assertions.assertThat(r.replayInfo()).contains("-Djrandomly.seed=");
        System.out.println("  ^^^ Copy the -D flags above to reproduce this run!");
        System.out.println("int(0-100)=" + r.intBetween(0, 100));
        System.out.println("alphaNumeric(8)=" + r.text().alphaNumeric(8));
        System.out.println("localDate()=" + r.dateTime().localDate());
        System.out.println("localTime()=" + r.dateTime().localTime());
        System.out.println("stockSymbol(DE)=" + r.finance().stockSymbol());

        // Run this test twice: the output will differ.
        // But if you take the replayInfo and set it as -D flags, it becomes deterministic.
    }

    private enum CarBrand {
        BMW, AUDI, MERCEDES, PORSCHE, TESLA, HONDA, TOYOTA, FORD
    }
}