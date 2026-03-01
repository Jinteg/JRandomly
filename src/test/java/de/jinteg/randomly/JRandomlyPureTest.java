package de.jinteg.randomly;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test verifies that the locale view of JRandomly is deterministic and does not affect the base stream.
 */
class JRandomlyPureTest {

    @AfterEach
    void cleanup() {
        System.clearProperty("jrandomly.seed");
        System.clearProperty("jrandomly.locale");
    }

    @Test
    void intBetween() {
        JRandomly r = JRandomly.randomly();
        int randomNumber = r.intBetween(0, 1_000);
        assertThat(randomNumber).isBetween(0, 1_000);
    }

    @Test
    void longBetween() {
        JRandomly r = JRandomly.randomly();
        long randomNumber = r.longBetween(0L, 10_000L);
        assertThat(randomNumber).isBetween(0L, 10_000L);
    }

    @Test
    void intWithExactDigits() {
        JRandomly r = JRandomly.randomly();
        int value = r.intWithExactDigits(2);
        assertThat(value).isBetween(10, 99);
    }

    @Test
    void intWithMaxDigits() {
        JRandomly r = JRandomly.randomly();
        int value = r.intWithMaxDigits(2);
        assertThat(value).isBetween(0, 99);
    }

    @Test
    void longWithExactDigits() {
        JRandomly r = JRandomly.randomly();
        long value = r.longWithExactDigits(5);
        assertThat(value).isBetween(10_000L, 99_999L);
    }

    @Test
    void longWithMaxDigits() {
        JRandomly r = JRandomly.randomly();
        long value = r.longWithMaxDigits(5);
        assertThat(value).isBetween(0L, 99_999L);
    }

    @Test
    void intWithExactDigitsBoundaryOneDigit() {
        JRandomly r = JRandomly.randomly();
        int value = r.intWithExactDigits(1);
        assertThat(value).isBetween(0, 9);
    }

    @Test
    void intWithExactDigitsBoundaryTenDigits() {
        JRandomly r = JRandomly.randomly();
        int value = r.intWithExactDigits(10);
        assertThat(value).isBetween(1_000_000_000, Integer.MAX_VALUE);
    }

    @Test
    void intWithExactDigitsRejectsInvalidDigits() {
        JRandomly r = JRandomly.randomly();
        assertThatThrownBy(() -> r.intWithExactDigits(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> r.intWithExactDigits(11))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void intWithMaxDigitsBoundaryOneDigit() {
        JRandomly r = JRandomly.randomly();
        int value = r.intWithMaxDigits(1);
        assertThat(value).isBetween(0, 9);
    }

    @Test
    void intWithMaxDigitsBoundaryTenDigits() {
        JRandomly r = JRandomly.randomly();
        int value = r.intWithMaxDigits(10);
        assertThat(value).isBetween(0, Integer.MAX_VALUE);
    }

    @Test
    void intWithMaxDigitsRejectsInvalidDigits() {
        JRandomly r = JRandomly.randomly();
        assertThatThrownBy(() -> r.intWithMaxDigits(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> r.intWithMaxDigits(11))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void longWithExactDigitsBoundaryOneDigit() {
        JRandomly r = JRandomly.randomly();
        long value = r.longWithExactDigits(1);
        assertThat(value).isBetween(0L, 9L);
    }

    @Test
    void longWithExactDigitsBoundaryNineteenDigits() {
        JRandomly r = JRandomly.randomly();
        long value = r.longWithExactDigits(19);
        assertThat(value).isBetween(1_000_000_000_000_000_000L, Long.MAX_VALUE);
    }

    @Test
    void longWithExactDigitsRejectsInvalidDigits() {
        JRandomly r = JRandomly.randomly();
        assertThatThrownBy(() -> r.longWithExactDigits(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> r.longWithExactDigits(20))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void longWithMaxDigitsBoundaryOneDigit() {
        JRandomly r = JRandomly.randomly();
        long value = r.longWithMaxDigits(1);
        assertThat(value).isBetween(0L, 9L);
    }

    @Test
    void longWithMaxDigitsBoundaryNineteenDigits() {
        JRandomly r = JRandomly.randomly();
        long value = r.longWithMaxDigits(19);
        assertThat(value).isBetween(0L, Long.MAX_VALUE);
    }

    @Test
    void longWithMaxDigitsRejectsInvalidDigits() {
        JRandomly r = JRandomly.randomly();
        assertThatThrownBy(() -> r.longWithMaxDigits(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> r.longWithMaxDigits(20))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void doubleBetween() {
        JRandomly r = JRandomly.randomly();
        double randomNumber = r.doubleBetween(-100.0, 100.0);
        assertThat(randomNumber).isBetween(-100.0, 100.0);
    }

    @Test
    void doubleBetween_with_decimal_places() {
        JRandomly r = JRandomly.randomly();
        double randomNumber = r.doubleBetween(5, -100.0, 100.0);

        String s = String.format(Locale.US, "%.5f", randomNumber);

        assertThat(randomNumber)
                .isBetween(-100.0, 100.0);

        assertThat(s.substring(s.indexOf('.') + 1))
                .hasSize(5);
    }

    @Test
    void list_pure_stream() {
        JRandomly randomly = JRandomly.randomly();
        List<String> ratings = List.of("AA", "BB", "CC");
        String actual = randomly.elementOf(ratings);
        assertThat(actual).isIn(ratings);
    }

    @Test
    void list_pure_excluding() {
        JRandomly randomly = JRandomly.randomly();
        List<String> ratings = List.of("AA", "BB", "CC");
        String actual = randomly.elementOf(ratings, Collections.singleton("BB"));
        assertThat(actual).isIn(ratings)
                .isNotEqualTo("BB");
    }

    @Test
    void list_pure_count_and_excluding() {
        JRandomly randomly = JRandomly.randomly();
        List<String> ratings = List.of("AA", "BB", "CC", "DD", "EE", "FF", "GG");
        List<String> actual = randomly.elementsOf(ratings, 2, List.of("BB", "CC"));

        assertThat(actual)
                .hasSize(2)
                .doesNotContain("BB", "CC")
                .isSubsetOf(ratings)
                .doesNotHaveDuplicates();
    }

    @Test
    void set_element_pure_count_and_excluding() {
        JRandomly randomly = JRandomly.randomly();
        Set<String> ratings = Set.of("AA", "BB", "CC", "DD", "EE", "FF", "GG");
        String actual = randomly.elementOf(ratings, List.of("BB", "CC"));
        assertThat(actual).isIn(ratings)
                .doesNotContain("BB", "CC");
    }

    @Test
    void set_elements_pure_count() {
        JRandomly randomly = JRandomly.randomly();
        Set<String> ratings = Set.of("AA", "BB", "CC", "DD", "EE", "FF", "GG");
        Set<String> actual = randomly.elementsOf(ratings, 2);

        assertThat(actual)
                .hasSize(2)
                .isSubsetOf(ratings)
                .doesNotHaveDuplicates();
    }

    @Test
    void set_elements_pure_count_and_excluding() {
        JRandomly randomly = JRandomly.randomly();
        Set<String> ratings = Set.of("AA", "BB", "CC", "DD", "EE", "FF", "GG");
        Set<String> actual = randomly.elementsOf(ratings, 2, List.of("BB", "CC"));

        assertThat(actual)
                .hasSize(2)
                .doesNotContain("BB", "CC")
                .isSubsetOf(ratings)
                .doesNotHaveDuplicates();
    }

    @Test
    void enums_pure_stream() {
        JRandomly randomly = JRandomly.randomly();

        SomeFruit fruit = randomly.enumOf(SomeFruit.class);
        assertThat(fruit).isIn((Object[]) SomeFruit.values());

        Optional<Enum.EnumDesc<SomeFruit>> someFruitEnumDesc = randomly.enumOf(SomeFruit.class).describeConstable();
        assertThat(someFruitEnumDesc).isPresent();
    }

    @Test
    void enums_pure_excluding() {
        JRandomly randomly = JRandomly.randomly();

        SomeFruit actual = randomly.enumOf(SomeFruit.class, Collections.singleton(SomeFruit.BANANA));
        assertThat(actual).isIn((Object[]) SomeFruit.values())
                .isNotEqualTo(SomeFruit.BANANA);
    }

    @Test
    void enums_pure_count() {
        JRandomly randomly = JRandomly.randomly();

        List<SomeFruit> actual = randomly.enumsOf(SomeFruit.class, 2);
        assertThat(actual).hasSize(2)
                .isSubsetOf(SomeFruit.values());
    }

    @Test
    void enums_pure_count_and_excluding() {
        JRandomly randomly = JRandomly.randomly();

        List<SomeFruit> actual = randomly.enumsOf(SomeFruit.class, 3, List.of(SomeFruit.BANANA, SomeFruit.DRAGON_FRUIT));
        assertThat(actual).hasSize(3)
                .doesNotContain(SomeFruit.BANANA, SomeFruit.DRAGON_FRUIT)
                .doesNotHaveDuplicates();
    }

    enum SomeFruit {
        APPLE,
        BANANA,
        CHERRY,
        DRAGON_FRUIT,
        FIG,
        GRAPE
    }
}
