package de.jinteg.randomly.domain.finance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TestFxRatesSample {

    @Test
    void usd_returns_one() {
        assertThat(SampleFxRates.rateForCurrency("USD")).isOne();
    }

    @Test
    void usd_is_case_insensitive() {
        assertThat(SampleFxRates.rateForCurrency("usd")).isOne();
    }

    @Test
    void eur_returns_expected_rate() {
        assertThat(SampleFxRates.rateForCurrency("EUR")).isCloseTo(0.92, within(0.01));
    }

    @Test
    void jpy_returns_expected_rate() {
        assertThat(SampleFxRates.rateForCurrency("JPY")).isCloseTo(154.50, within(1.0));
    }

    @Test
    void unsupported_currency_throws() {
        assertThatThrownBy(() -> SampleFxRates.rateForCurrency("XYZ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No test FX rate available")
                .hasMessageContaining("XYZ");
    }
}