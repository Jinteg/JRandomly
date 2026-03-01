package de.jinteg.randomly.domain.finance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class StockEntryTest {

    @Test
    void parses_US_stock_correctly() {
        // given
        String line = "NVDA|NVIDIA Corporation|2.18T|138.77|USD|US67066G1040|XNAS";
        String[] parts = line.split("\\|");

        // when
        StockEntry stockEntry = StockEntry.parse(parts);

        // then
        assertThat(stockEntry).isNotNull();
        assertThat(stockEntry.symbol()).isEqualTo("NVDA");
        assertThat(stockEntry.name()).isEqualTo("NVIDIA Corporation");
        assertThat(stockEntry.marketCap()).isEqualTo(2180000000000L);
        assertThat(stockEntry.price()).isEqualTo(138.77);
        assertThat(stockEntry.currencyCode()).isEqualTo("USD");
        assertThat(stockEntry.isin()).isEqualTo("US67066G1040");
        assertThat(stockEntry.mic()).isEqualTo("XNAS");
    }

    @Test
    void parses_DE_stock_correctly() {
        // given
        String line = "BMW|Bayerische Motoren Werke Aktiengesellschaft|68.3B|102.44|EUR|DE0005190003|XETR";
        String[] parts = line.split("\\|");

        // when
        StockEntry stockEntry = StockEntry.parse(parts);

        // then
        assertThat(stockEntry).isNotNull();
        assertThat(stockEntry.symbol()).isEqualTo("BMW");
        assertThat(stockEntry.name()).isEqualTo("Bayerische Motoren Werke Aktiengesellschaft");
        assertThat(stockEntry.marketCap()).isEqualTo(68300000000L);
        assertThat(stockEntry.price()).isEqualTo(102.44);
        assertThat(stockEntry.currencyCode()).isEqualTo("EUR");
        assertThat(stockEntry.isin()).isEqualTo("DE0005190003");
        assertThat(stockEntry.mic()).isEqualTo("XETR");
    }
}