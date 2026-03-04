package de.jinteg.randomly.domain.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Internal parser for raw crypto asset catalog lines.
 */
final class CryptoAssetParser {

    static final int COLUMN_COUNT = 5;

    private CryptoAssetParser() {
        // utility class
    }

    /**
     * Parse a raw catalog line and convert to the given quote currency.
     *
     * @param parts             raw parts: SYMBOL|AssetName|PriceUSD|MarketCapUSD|Network
     * @param quoteCurrencyCode target quote currency
     * @return CryptoAssetEntry with converted price and marketCap
     */
    static CryptoAssetPick parse(String[] parts, String quoteCurrencyCode) {
        if (parts == null || parts.length != COLUMN_COUNT) {
            throw new IllegalArgumentException("Invalid crypto raw data: " + Arrays.toString(parts)
                    + ", expected exactly " + COLUMN_COUNT + " parts");
        }

        String baseSymbol = parts[0].trim();
        String assetName = parts[1].trim();
        double priceUsd = Double.parseDouble(parts[2].trim());
        long marketCapUsd = MonetaryMagnitudeParser.parse(parts[3].trim());
        String network = parts[4].trim();

        double fxRate = SampleFxRates.rateForCurrency(quoteCurrencyCode);

        double convertedPrice = BigDecimal.valueOf(priceUsd)
                .multiply(BigDecimal.valueOf(fxRate))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        long convertedMarketCap = BigDecimal.valueOf(marketCapUsd)
                .multiply(BigDecimal.valueOf(fxRate))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        String pairSymbol = baseSymbol + "-" + quoteCurrencyCode;

        return new CryptoAssetPick(
                pairSymbol,
                baseSymbol,
                assetName,
                convertedPrice,
                quoteCurrencyCode,
                convertedMarketCap,
                network
        );
    }
}