package de.jinteg.randomly.domain.finance;

import de.jinteg.randomly.JRandomly;
import de.jinteg.randomly.internal.catalog.CatalogEntryLookup;
import de.jinteg.randomly.internal.catalog.NumberedPropertiesCatalog;
import de.jinteg.randomly.internal.catalog.RawParserUtil;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Locale.ENGLISH;

/**
 * Provides finance-related random data, such as stock symbols and stock entries.
 */
public final class FinanceRandomly {

  private static final String STOCK_CATALOG_PATH = "de/jinteg/randomly/catalog/finance/stocks";
  private static final String CRYPTO_ASSET_CATALOG_PATH = "de/jinteg/randomly/catalog/finance/crypto_assets";

  /**
   * Fixed, ordered list of widely used ISO 4217 currencies.
   *
   * <p>Not derived from {@link Currency#getAvailableCurrencies()}: that set has no defined
   * order and its content changes with JDK updates, which would break reproducibility
   * (ADR-0008).
   */
  private static final List<Currency> AVAILABLE_CURRENCIES = Stream.of(
          "AED", "ARS", "AUD", "BRL", "CAD", "CHF", "CLP", "CNY", "COP", "CZK", "DKK", "EGP",
          "EUR", "GBP", "HKD", "HUF", "IDR", "ILS", "INR", "JPY", "KRW", "MXN", "MYR", "NOK",
          "NZD", "PHP", "PLN", "SAR", "SEK", "SGD", "THB", "TRY", "TWD", "USD", "VND", "ZAR")
      .map(Currency::getInstance)
      .toList();

  private final JRandomly randomly;

  /**
   * Constructor.
   *
   * @param randomly random number generator
   */
  public FinanceRandomly(JRandomly randomly) {
    this.randomly = Objects.requireNonNull(randomly, "randomly must not be null");
  }

  private static boolean hasLeadingColumn(String rawEntry, String expectedValue) {
    int separatorIndex = rawEntry.indexOf('|');
    if (separatorIndex < 0) {
      return false;
    }
    return rawEntry.substring(0, separatorIndex).trim().equals(expectedValue);
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
   * Returns a random, consistent stock pick (symbol, companyName, market cap, price,
   * currency code, ISIN, MIC).
   *
   * @return stock pick
   */
  public StockPick stock() {
    return stock(randomly.getLocale());
  }

  /**
   * Returns the exact stock entry for the given symbol using the configured locale.
   *
   * @param symbol stock symbol
   * @return stock pick
   */
  public StockPick stockBySymbol(String symbol) {
    return stockBySymbol(symbol, randomly.getLocale());
  }

  /**
   * Returns the exact stock entry for the given symbol and locale.
   *
   * @param symbol stock symbol
   * @param locale locale to use for catalog selection
   * @return stock pick
   */
  public StockPick stockBySymbol(String symbol, Locale locale) {
    Objects.requireNonNull(symbol, "symbol");
    Objects.requireNonNull(locale, "locale");

    String normalizedSymbol = symbol.trim();

    String raw = CatalogEntryLookup.firstMatchingEntry(
        STOCK_CATALOG_PATH,
        locale,
        entry -> hasLeadingColumn(entry, normalizedSymbol),
        "Stock symbol %s not found for locale %s"
            .formatted(normalizedSymbol, locale.getLanguage())
    );
    return StockPick.parse(RawParserUtil.parse(raw, StockPick.COLUMN_COUNT));
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
   * Returns the exact crypto asset entry for the given symbol quoted in the currency
   * derived from the configured locale.
   *
   * @param symbol crypto asset symbol
   * @return crypto asset pick in locale-specific currency
   */
  public CryptoAssetPick cryptoAssetBySymbol(String symbol) {
    return cryptoAssetBySymbol(symbol, getLocaleCurrencyCode());
  }

  /**
   * Returns the exact crypto asset entry for the given symbol quoted in the given currency.
   *
   * @param symbol            crypto asset symbol
   * @param quoteCurrencyCode ISO 4217 currency code (e.g. "EUR", "USD", "JPY")
   * @return crypto asset entry with converted price and market cap
   */
  public CryptoAssetPick cryptoAssetBySymbol(String symbol, String quoteCurrencyCode) {
    Objects.requireNonNull(symbol, "symbol");
    Objects.requireNonNull(quoteCurrencyCode, "quoteCurrencyCode");

    String normalizedSymbol = symbol.trim();

    String raw = CatalogEntryLookup.firstMatchingEntry(
        CRYPTO_ASSET_CATALOG_PATH,
        ENGLISH,
        entry -> hasLeadingColumn(entry, normalizedSymbol),
        "Crypto asset symbol %s not found".formatted(normalizedSymbol)
    );
    return CryptoAssetParser.parse(
        RawParserUtil.parse(raw, CryptoAssetParser.COLUMN_COUNT),
        quoteCurrencyCode
    );
  }

  /**
   * Returns a random, consistent stock pick (symbol, companyName, market cap, price,
   * currency code, ISIN, MIC).
   *
   * @param locale locale to use for catalog selection
   * @return picked stock
   */
  public StockPick stock(Locale locale) {
    Objects.requireNonNull(locale, "locale");
    List<String> entries = NumberedPropertiesCatalog.loadList(STOCK_CATALOG_PATH, locale);
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
   * Returns a random currency symbol as displayed in the configured locale.
   *
   * @return currency symbol, e.g. "$", "€", or "£"
   */
  public String currencySymbol() {
    return currency().getSymbol(randomly.getLocale());
  }

  /**
   * Returns a random currency symbol, excluding the specified symbols.
   *
   * @param excluding currency symbols to exclude
   * @return random currency symbol
   */
  public String currencySymbol(Collection<String> excluding) {
    Objects.requireNonNull(excluding, "excluding");
    List<String> filtered = AVAILABLE_CURRENCIES.stream()
        .map(currency -> currency.getSymbol(randomly.getLocale()))
        .filter(symbol -> !excluding.contains(symbol))
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
    List<String> entries = NumberedPropertiesCatalog.loadList(CRYPTO_ASSET_CATALOG_PATH, ENGLISH);
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