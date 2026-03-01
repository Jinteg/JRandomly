package de.jinteg.randomly.core;

import de.jinteg.randomly.JRandomly;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class IdRandomlyTest {

    private final JRandomly randomly = JRandomly.randomly("IdRandomlyTest");

    @Test
    void uuid() {
        UUID uuid = randomly.id().uuid();
        Assertions.assertThat(uuid).isNotNull();
        Assertions.assertThat(uuid.toString()).hasSize(36);
    }

    @Test
    void longId() {
        long l = randomly.id().longId();
        Assertions.assertThat(l).isGreaterThanOrEqualTo(0)
                .isLessThanOrEqualTo(Long.MAX_VALUE);
    }

    @Test
    void longIdBetween() {
        long l = randomly.id().longIdBetween(1000, 2000);
        Assertions.assertThat(l).isGreaterThanOrEqualTo(1000)
                .isLessThan(2000);
    }

    @Test
    void intId() {
        int i = randomly.id().intId();
        Assertions.assertThat(i).isGreaterThanOrEqualTo(0)
                .isLessThan(Integer.MAX_VALUE);
    }

    @Test
    void intIdBetween() {
        int i = randomly.id().intIdBetween(1000, 2000);
        Assertions.assertThat(i).isGreaterThanOrEqualTo(1000)
                .isLessThan(2000);
    }

    @Test
    void intIdWithExactDigits() {
        int i = randomly.id().intIdWithExactDigits(2);
        Assertions.assertThat(i).isBetween(10, 99);
    }

    @Test
    void intIdWithMaxDigits() {
        int i = randomly.id().intIdWithMaxDigits(2);
        Assertions.assertThat(i).isBetween(1, 99);
    }

    @Test
    void longIdWithExactDigits() {
        long l = randomly.id().longIdWithExactDigits(5);
        Assertions.assertThat(l).isBetween(10_000L, 99_999L);
    }

    @Test
    void longIdWithMaxDigits() {
        long l = randomly.id().longIdWithMaxDigits(5);
        Assertions.assertThat(l).isBetween(1L, 99_999L);
    }


    @Test
    void intIdWithExactDigitsBoundaryOneDigit() {
        int i = randomly.id().intIdWithExactDigits(1);
        Assertions.assertThat(i).isBetween(1, 9);
    }

    @Test
    void intIdWithExactDigitsBoundaryTenDigits() {
        int i = randomly.id().intIdWithExactDigits(10);
        Assertions.assertThat(i).isBetween(1_000_000_000, Integer.MAX_VALUE);
    }

    @Test
    void intIdWithExactDigitsRejectsInvalidDigits() {
        assertThatThrownBy(() -> randomly.id().intIdWithExactDigits(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> randomly.id().intIdWithExactDigits(11))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void intIdWithMaxDigitsBoundaryOneDigit() {
        int i = randomly.id().intIdWithMaxDigits(1);
        Assertions.assertThat(i).isBetween(1, 9);
    }

    @Test
    void intIdWithMaxDigitsBoundaryTenDigits() {
        int i = randomly.id().intIdWithMaxDigits(10);
        Assertions.assertThat(i).isBetween(1, Integer.MAX_VALUE);
    }

    @Test
    void intIdWithMaxDigitsRejectsInvalidDigits() {
        assertThatThrownBy(() -> randomly.id().intIdWithMaxDigits(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> randomly.id().intIdWithMaxDigits(11))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void longIdWithExactDigitsBoundaryOneDigit() {
        long l = randomly.id().longIdWithExactDigits(1);
        Assertions.assertThat(l).isBetween(1L, 9L);
    }

    @Test
    void longIdWithExactDigitsBoundaryNineteenDigits() {
        long l = randomly.id().longIdWithExactDigits(19);
        Assertions.assertThat(l).isBetween(1_000_000_000_000_000_000L, Long.MAX_VALUE);
    }

    @Test
    void longIdWithExactDigitsRejectsInvalidDigits() {
        assertThatThrownBy(() -> randomly.id().longIdWithExactDigits(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> randomly.id().longIdWithExactDigits(20))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void longIdWithMaxDigitsBoundaryOneDigit() {
        long l = randomly.id().longIdWithMaxDigits(1);
        Assertions.assertThat(l).isBetween(1L, 9L);
    }

    @Test
    void longIdWithMaxDigitsBoundaryNineteenDigits() {
        long l = randomly.id().longIdWithMaxDigits(19);
        Assertions.assertThat(l).isBetween(1L, Long.MAX_VALUE);
    }

    @Test
    void longIdWithMaxDigitsRejectsInvalidDigits() {
        assertThatThrownBy(() -> randomly.id().longIdWithMaxDigits(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> randomly.id().longIdWithMaxDigits(20))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void key() {
        String key = randomly.id().key(16);
        Assertions.assertThat(key)
                .hasSize(16)
                .matches("^[A-Za-z0-9]+$");
    }

    @Test
    void keyRejectsNonPositiveLength() {
        assertThatThrownBy(() -> randomly.id().key(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> randomly.id().key(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void numericKey() {
        String key = randomly.id().numericKey(16);
        Assertions.assertThat(key)
                .hasSize(16)
                .matches("^\\d+$");
    }

    @Test
    void numericKeyRejectsNonPositiveLength() {
        assertThatThrownBy(() -> randomly.id().numericKey(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> randomly.id().numericKey(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void prefixedId() {
        String prefix = "PREFIX";
        String id = randomly.id().prefixedId(prefix, 10);
        Assertions.assertThat(id).startsWith(prefix)
                .hasSize(10);
    }
}