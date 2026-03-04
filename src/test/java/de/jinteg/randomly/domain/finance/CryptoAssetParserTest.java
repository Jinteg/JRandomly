package de.jinteg.randomly.domain.finance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CryptoAssetParserTest {

    @Test
    void parses_btc_in_usd() {
        // given
        String[] parts = "BTC|Bitcoin|67500.00|1.32T|Bitcoin".split("\\|");

        // when
        CryptoAssetPick pick = CryptoAssetParser.parse(parts, "USD");

        // then
        assertThat(pick.pairSymbol()).isEqualTo("BTC-USD");
        assertThat(pick.baseSymbol()).isEqualTo("BTC");
        assertThat(pick.assetName()).isEqualTo("Bitcoin");
        assertThat(pick.price()).isEqualTo(67500.00);
        assertThat(pick.quoteCurrencyCode()).isEqualTo("USD");
        assertThat(pick.marketCap()).isEqualTo(1_320_000_000_000L);
        assertThat(pick.network()).isEqualTo("Bitcoin");
    }

    @Test
    void converts_eth_to_eur() {
        // given
        String[] parts = "ETH|Ethereum|3420.00|411B|Ethereum".split("\\|");

        // when
        CryptoAssetPick pick = CryptoAssetParser.parse(parts, "EUR");

        // then
        assertThat(pick.pairSymbol()).isEqualTo("ETH-EUR");
        assertThat(pick.baseSymbol()).isEqualTo("ETH");
        assertThat(pick.quoteCurrencyCode()).isEqualTo("EUR");
        // 3420.00 * 0.92 = 3146.40
        assertThat(pick.price()).isCloseTo(3146.40, within(0.01));
        // 411_000_000_000 * 0.92 = 378_120_000_000
        assertThat(pick.marketCap()).isCloseTo(378_120_000_000L, within(1_000_000L));
        assertThat(pick.network()).isEqualTo("Ethereum");
    }

    @Test
    void converts_sol_to_jpy() {
        // given
        String[] parts = "SOL|Solana|145.00|63B|Solana".split("\\|");

        // when
        CryptoAssetPick entry = CryptoAssetParser.parse(parts, "JPY");

        // then
        assertThat(entry.pairSymbol()).isEqualTo("SOL-JPY");
        // 145.00 * 154.50 = 22402.50
        assertThat(entry.price()).isCloseTo(22402.50, within(0.01));
        assertThat(entry.quoteCurrencyCode()).isEqualTo("JPY");
    }

    @Test
    void rejects_invalid_column_count() {
        String[] parts = "BTC|Bitcoin|67500.00".split("\\|");

        assertThatThrownBy(() -> CryptoAssetParser.parse(parts, "USD"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid crypto raw data");
    }

    @Test
    void rejects_unsupported_quote_currency() {
        String[] parts = "BTC|Bitcoin|67500.00|1.32T|Bitcoin".split("\\|");

        assertThatThrownBy(() -> CryptoAssetParser.parse(parts, "XYZ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No test FX rate available");
    }
}