package de.jinteg.randomly.domain.finance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class MonetaryMagnitudeParserTest {

    @Test
    void parses_market_cap_with_million_suffix() {
        assertThat(MonetaryMagnitudeParser.parse("125M")).isEqualTo(125_000_000L);
    }

    @Test
    void parses_market_cap_with_decimal_and_comma() {
        assertThat(MonetaryMagnitudeParser.parse("1,25B")).isEqualTo(1_250_000_000L);
    }

    @Test
    void parses_market_cap_without_suffix() {
        assertThat(MonetaryMagnitudeParser.parse("4200000")).isEqualTo(4_200_000L);
    }

    @Test
    void rejects_unsupported_market_cap_suffix() {
        assertThatThrownBy(() -> MonetaryMagnitudeParser.parse("10K"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported marketCap suffix");
    }

    @Test
    void rejects_blank_market_cap() {
        assertThatThrownBy(() -> MonetaryMagnitudeParser.parse("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null or blank");
    }


}