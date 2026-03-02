package de.jinteg.randomly.domain.finance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class StockPickTest {

    @Test
    void parses_US_stock_correctly() {
        // given
        String line = "NVDA|NVIDIA Corporation|2.18T|138.77|USD|US67066G1040|XNAS";
        String[] parts = line.split("\\|");

        // when
        StockPick stockPick = StockPick.parse(parts);

        // then
        assertThat(stockPick).isNotNull();
        assertThat(stockPick.symbol()).isEqualTo("NVDA");
        assertThat(stockPick.companyName()).isEqualTo("NVIDIA Corporation");
        assertThat(stockPick.marketCap()).isEqualTo(2180000000000L);
        assertThat(stockPick.price()).isEqualTo(138.77);
        assertThat(stockPick.currencyCode()).isEqualTo("USD");
        assertThat(stockPick.isin()).isEqualTo("US67066G1040");
        assertThat(stockPick.mic()).isEqualTo("XNAS");
    }

    @Test
    void parses_DE_stock_correctly() {
        // given
        String line = "BMW|Bayerische Motoren Werke Aktiengesellschaft|68.3B|102.44|EUR|DE0005190003|XETR";
        String[] parts = line.split("\\|");

        // when
        StockPick stockPick = StockPick.parse(parts);

        // then
        assertThat(stockPick).isNotNull();
        assertThat(stockPick.symbol()).isEqualTo("BMW");
        assertThat(stockPick.companyName()).isEqualTo("Bayerische Motoren Werke Aktiengesellschaft");
        assertThat(stockPick.marketCap()).isEqualTo(68300000000L);
        assertThat(stockPick.price()).isEqualTo(102.44);
        assertThat(stockPick.currencyCode()).isEqualTo("EUR");
        assertThat(stockPick.isin()).isEqualTo("DE0005190003");
        assertThat(stockPick.mic()).isEqualTo("XETR");
    }
}