package de.jinteg.randomly.domain.finance;

import de.jinteg.randomly.JRandomly;
import de.jinteg.randomly.internal.catalog.NumberedPropertiesCatalog;
import de.jinteg.randomly.internal.catalog.RawParserUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FinanceRandomlyTest {

    private static List<String> allowedSymbolsFor(Locale locale) {
        return NumberedPropertiesCatalog.loadList(
                        "de/jinteg/randomly/catalog/finance/stocks",
                        locale
                ).stream()
                .map(raw -> StockPick.parse(RawParserUtil.parse(raw, StockPick.COLUMN_COUNT)).symbol())
                .toList();
    }

    private static final List<String> ALLOWED_US_SYMBOLS = allowedSymbolsFor(Locale.US);
    private static final List<String> ALLOWED_DE_SYMBOLS = allowedSymbolsFor(Locale.GERMANY);

    @AfterEach
    void cleanup() {
        System.clearProperty("jrandomly.seed");
        System.clearProperty("jrandomly.locale");
    }

    @Test
    void stockSymbol_usesConfiguredLocaleByDefault() {
        System.setProperty("jrandomly.seed", "1");
        System.setProperty("jrandomly.locale", "de-DE");

        JRandomly r = JRandomly.randomly("FinanceTest#de");
        String sym = r.finance().stockSymbol();

        assertThat(sym).isIn(ALLOWED_DE_SYMBOLS);
    }

    @Test
    void stockSymbol_canBeOverriddenPerCallChainWithLocaleView() {
        System.setProperty("jrandomly.seed", "1");
        System.setProperty("jrandomly.locale", "de-DE");

        JRandomly r = JRandomly.randomly("FinanceTest#override");
        String sym = r.finance().stockSymbol(Locale.US);

        assertThat(sym).isIn(ALLOWED_US_SYMBOLS);
    }

    @Test
    void stock() {
        // given
        System.setProperty("jrandomly.seed", "1");
        System.setProperty("jrandomly.locale", "de-DE");

        // when
        JRandomly r = JRandomly.randomly("FinanceTest#stockPick");
        StockPick stockPick = r.finance().stock(Locale.US);

        // then
        assertThat(stockPick).isNotNull();
        assertThat(stockPick.symbol()).isIn(ALLOWED_US_SYMBOLS);
        assertThat(stockPick.companyName()).isNotBlank();
        assertThat(stockPick.marketCap()).isPositive();
        assertThat(stockPick.price()).isPositive();
        assertThat(stockPick.currencyCode()).isEqualTo("USD");
        assertThat(stockPick.mic()).isNotEmpty();
    }


    @Test
    void stock_withLocale() {
        System.setProperty("jrandomly.seed", "1");
        System.setProperty("jrandomly.locale", "de-DE");

        JRandomly r = JRandomly.randomly("FinanceTest#stockPickLocale");
        StockPick stockPick = r.finance().stock(Locale.GERMANY);
        assertThat(stockPick).isNotNull();
        assertThat(stockPick.symbol()).isIn(ALLOWED_DE_SYMBOLS);
        assertThat(stockPick.companyName()).isNotBlank();
        assertThat(stockPick.marketCap()).isPositive();
        assertThat(stockPick.price()).isPositive();
    }

    @Test
    void stock_without_Locale_uses_default_locale() {
        System.setProperty("jrandomly.seed", "1");
        System.setProperty("jrandomly.locale", "de-DE");

        JRandomly r = JRandomly.randomly("FinanceTest#stockPickWOLocale");
        StockPick stockPick = r.finance().stock();
        assertThat(stockPick).isNotNull();
        assertThat(stockPick.symbol()).isIn(ALLOWED_DE_SYMBOLS);
        assertThat(stockPick.companyName()).isNotBlank();
        assertThat(stockPick.marketCap()).isPositive();
        assertThat(stockPick.price()).isPositive();
    }

    @Test
    void stock_withUnsupportedLocale() {
        System.setProperty("jrandomly.seed", "1");

        JRandomly r = JRandomly.randomly("FinanceTest#stockPickLocaleUnsupported");

        assertThatThrownBy(() -> r.finance().stock(Locale.ITALY))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Catalog not found")
                .hasMessageContaining("it");
    }

    @Test
    void currencyApi_returns_valid_values() {
        JRandomly r = JRandomly.randomly("FinanceTest#Currency");
        Currency currency = r.finance().currency();
        String currencyCode = r.finance().currencyCode();
        String currencySymbol = r.finance().currencySymbol();

        assertThat(currency).isNotNull();
        assertThat(currencyCode).hasSize(3);
        assertThat(currencySymbol).isNotEmpty();
    }

    @Test
    void currency_with_excluding() {
        JRandomly r = JRandomly.randomly("FinanceTest#Currency");
        List<Currency> excluding = List.of(Currency.getInstance("EUR"), Currency.getInstance("USD"));
        Currency currency = r.finance().currency(excluding);

        assertThat(currency).isNotNull()
                .isNotEqualTo(Currency.getInstance("EUR"))
                .isNotEqualTo(Currency.getInstance("USD"));
    }

    @Test
    void currencyCode_excluding() {
        JRandomly r = JRandomly.randomly("FinanceTest#CurrencyCodeExcluding");
        String currency1 = r.finance().currencyCode();
        String currency2 = r.finance().currencyCode(List.of(currency1));

        assertThat(currency1).isNotNull().isNotEqualTo(currency2);
    }

    @Test
    void currencySymbol_excluding() {
        JRandomly r = JRandomly.randomly("FinanceTest#CurrencySymbolExcluding");
        String currency1 = r.finance().currencySymbol();
        String currency2 = r.finance().currencySymbol(List.of(currency1));

        assertThat(currency1).isNotNull().isNotEqualTo(currency2);
    }
}

