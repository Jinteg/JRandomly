package de.jinteg.randomly.domain.finance;

import de.jinteg.randomly.JRandomly;
import de.jinteg.randomly.internal.catalog.NumberedPropertiesCatalog;
import de.jinteg.randomly.internal.catalog.RawParserUtil;

import java.util.*;

/**
 * Provides finance-related random data, such as stock symbols and stock entries.
 */
public final class FinanceRandomly {
    private final JRandomly randomly;
    private static final List<Currency> AVAILABLE_CURRENCIES = List.copyOf(Currency.getAvailableCurrencies());
    private static final String CRYPTO_CATALOG = "de/jinteg/randomly/catalog/finance/crypto_assets";


    /**
     * Constructor.
     *
     * @param randomly random number generator
     */
    public FinanceRandomly(JRandomly randomly) {
        this.randomly = Objects.requireNonNull(randomly, "randomly must not be null");
    }


    /**
     * Returns a stock symbol using the locale of the given JRandomly instance.
     *
     * @return stock symbol
     */
    public String stockSymbol() {
        return stockSymbol(randomly.getLocale());
    }

    /**
     * Returns a stock symbol using the given locale for catalog selection.
     *
     * @param locale locale to use for catalog selection
     * @return stock symbol
     */
    public String stockSymbol(Locale locale) {
        return stock(locale).symbol();
    }

    /**
     * Returns a random, consistent stock pick (symbol, companyName, market cap, price, currency code, ISIN, MIC).
     *
     * @return stock pick
     */
    public StockPick stock() {
        return stock(randomly.getLocale());
    }

    /**
     * Returns a random crypto asset entry quoted in the currency
     * derived from the configured locale.
     *
     * @return crypto asset pick in locale-specific currency
     */
    public CryptoAssetPick cryptoAsset() {
        return cryptoAsset(getLocaleCurrencyCode());
    }

    /**
     * Returns a random, consistent stock pick (symbol, companyName, market cap, price, currency code, ISIN, MIC).
     *
     * @param locale locale to use for catalog selection
     * @return picked stock
     */
    public StockPick stock(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        List<String> entries = NumberedPropertiesCatalog.loadList(
                "de/jinteg/randomly/catalog/finance/stocks",
                locale
        );
        String raw = entries.get(randomly.index(entries.size()));
        return StockPick.parse(RawParserUtil.parse(raw, StockPick.COLUMN_COUNT));
    }

    /**
     * Returns a random currency.
     *
     * @return random currency
     */
    public Currency currency() {
        return AVAILABLE_CURRENCIES.get(randomly.index(AVAILABLE_CURRENCIES.size()));
    }

    /**
     * Returns a random currency, excluding the specified ones.
     *
     * @param excluding currencies to exclude
     * @return random currency
     */
    public Currency currency(Collection<Currency> excluding) {
        return randomly.elementOf(AVAILABLE_CURRENCIES, excluding);
    }

    /**
     * Returns a random ISO 4217 currency code -e.g. "USD", "EUR", "CHF"
     *
     * @return random currency code
     */
    public String currencyCode() {
        return currency().getCurrencyCode();
    }

    /**
     * Returns a random ISO 4217 currency code, excluding the specified codes.
     *
     * @param excluding currency codes to exclude
     * @return random currency code
     */
    public String currencyCode(Collection<String> excluding) {
        Objects.requireNonNull(excluding, "excluding");
        List<String> filtered = AVAILABLE_CURRENCIES.stream()
                .map(Currency::getCurrencyCode)
                .filter(code -> !excluding.contains(code))
                .toList();
        return randomly.elementOf(filtered);
    }

    /**
     * currency symbol
     *
     * @return Returns one char currency symbol, e.g. "$", "€", "£"
     */
    public String currencySymbol() {
        return currency().getSymbol();
    }

    /**
     * Returns a random currency code, excluding the specified codes.
     *
     * @param excluding currency codes to exclude
     * @return random currency code
     */
    public String currencySymbol(Collection<String> excluding) {
        Objects.requireNonNull(excluding, "excluding");
        List<String> filtered = AVAILABLE_CURRENCIES.stream()
                .map(Currency::getSymbol)
                .filter(code -> !excluding.contains(code))
                .toList();
        return randomly.elementOf(filtered);
    }

    /**
     * Derives the ISO 4217 currency code from the configured locale.
     *
     * @return currency code (e.g. "EUR", "USD", "JPY")
     */
    private String getLocaleCurrencyCode() {
        try {
            return Currency.getInstance(randomly.getLocale()).getCurrencyCode();
        } catch (IllegalArgumentException e) {
            // Fallback for locales without country (e.g. "en", "de")
            return "USD";
        }
    }

    /**
     * Returns a random crypto asset entry quoted in the given currency.
     *
     * @param quoteCurrencyCode ISO 4217 currency code (e.g. "EUR", "USD", "JPY")
     * @return crypto asset entry with converted price and market cap
     */
    public CryptoAssetPick cryptoAsset(String quoteCurrencyCode) {
        Objects.requireNonNull(quoteCurrencyCode, "quoteCurrencyCode");
        List<String> entries = NumberedPropertiesCatalog.loadList(CRYPTO_CATALOG, Locale.ENGLISH);
        String raw = entries.get(randomly.index(entries.size()));
        return CryptoAssetParser.parse(
                RawParserUtil.parse(raw, CryptoAssetParser.COLUMN_COUNT),
                quoteCurrencyCode
        );
    }

    /**
     * Returns a crypto trading pair symbol (e.g. "BTC-USD", "ETH-EUR").
     *
     * @return pair symbol
     */
    public String cryptoPairSymbol() {
        return cryptoAsset().pairSymbol();
    }

    /**
     * Returns a crypto trading pair symbol in the given quote currency.
     *
     * @param quoteCurrencyCode quote currency
     * @return pair symbol
     */
    public String cryptoPairSymbol(String quoteCurrencyCode) {
        return cryptoAsset(quoteCurrencyCode).pairSymbol();
    }

}