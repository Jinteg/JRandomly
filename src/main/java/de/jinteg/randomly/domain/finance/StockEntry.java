package de.jinteg.randomly.domain.finance;

import java.util.Arrays;

/**
 * Stock entry with symbol, companyName, market cap, and price.
 *
 * @param symbol       stock symbol
 * @param companyName  stock companyName
 * @param marketCap    market capitalization
 * @param price        stock price
 * @param currencyCode stock currency code e.g. "USD", "EUR", "CHF"
 * @param isin         stock ISIN code
 * @param mic          stock MIC code
 */
public record StockEntry(
        String symbol,
        String companyName,
        long marketCap,
        double price,
        String currencyCode,
        String isin,
        String mic
) {
    /**
     * Number of pipe-delimited columns expected in the raw catalog format.
     */
    static final int COLUMN_COUNT = 7;

    /**
     * Parse a pipe-delimited catalog line into a {@link StockEntry}.
     *
     * @param parts in format "SYMBOL|Name|MarketCap|Price|Currency|ISIN|MIC"
     * @return parsed entry
     */
    static StockEntry parse(String[] parts) {
        if (parts == null || parts.length != COLUMN_COUNT) {
            throw new IllegalArgumentException("Invalid stock raw data: " + Arrays.toString(parts)
                    + ", expected exactly " + COLUMN_COUNT
                    + " parts separated by '|': SYMBOL|Name|MarketCap|Price|Currency|ISIN|MIC");
        }
        return new StockEntry(
                parts[0].trim(),
                parts[1].trim(),
                MonetaryMagnitudeParser.parse(parts[2].trim()),
                Double.parseDouble(parts[3].trim()),
                parts[4].trim(),
                parts[5].trim(),
                parts[6].trim()
        );
    }
}