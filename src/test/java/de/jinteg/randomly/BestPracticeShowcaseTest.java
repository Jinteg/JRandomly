package de.jinteg.randomly;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Best Practice Showcase")
class BestPracticeShowcaseTest {

    @AfterEach
    void cleanupSystemProperties() {
        System.clearProperty("jrandomly.seed");
        System.clearProperty("jrandomly.runStartTime");
        System.clearProperty("jrandomly.locale");
        System.clearProperty("jrandomly.maybeRate");
    }

    @Test
    @DisplayName("Uses explicit locale via builder")
    void usesExplicitLocaleViaBuilder() {
        JRandomly randomly = JRandomly.builder()
                .withLocale(Locale.GERMANY)
                .build();

        assertThat(randomly.getLocale()).isEqualTo(Locale.GERMANY);
    }

    @Test
    @DisplayName("Produces deterministic values with fixed seed and scope")
    void producesDeterministicValuesWithFixedSeedAndScope() {
        JRandomly first = JRandomly.builder()
                .withSeed(123456L)
                .withLocale(Locale.US)
                .withScope("BestPractice#deterministic")
                .build();

        JRandomly second = JRandomly.builder()
                .withSeed(123456L)
                .withLocale(Locale.US)
                .withScope("BestPractice#deterministic")
                .build();

        assertThat(first.intBetween(0, 1000)).isEqualTo(second.intBetween(0, 1000));
        assertThat(first.text().alphaNumeric(12)).isEqualTo(second.text().alphaNumeric(12));
        assertThat(first.bool()).isEqualTo(second.bool());
    }

    @Test
    @DisplayName("Replay info contains explicit configuration")
    void replayInfoContainsExplicitConfiguration() {
        Instant runStartTime = Instant.parse("2026-02-07T12:30:56Z");

        JRandomly randomly = JRandomly.builder()
                .withSeed(123456L)
                .withLocale(Locale.GERMANY)
                .withRunStartTime(runStartTime)
                .withMaybeRate(0.35)
                .withScope("BestPractice#replay")
                .build();

        assertThat(randomly.replayInfo())
                .contains("-Djrandomly.seed=123456")
                .contains("-Djrandomly.runStartTime=2026-02-07T12:30:56Z")
                .contains("-Djrandomly.locale=de-DE")
                .contains("-Djrandomly.maybeRate=0.35");
    }

    @Test
    @DisplayName("Uses explicit locale for finance catalog selection")
    void usesExplicitLocaleForFinanceCatalogSelection() {
        JRandomly randomly = JRandomly.builder()
                .withSeed(123456L)
                .withLocale(Locale.GERMANY)
                .withScope("BestPractice#finance")
                .build();

        String stockSymbol = randomly.finance().stockSymbol();

        assertThat(stockSymbol).isNotBlank();
        assertThat(randomly.getLocale()).isEqualTo(Locale.GERMANY);
    }
}