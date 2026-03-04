package de.jinteg.randomly.domain.finance;

/**
 * Crypto asset entry with trading pair, identity, and approximate market data.
 *
 * <p>Prices and market caps are stored in USD internally and converted
 * at query time using static test FX rates.
 *
 * @param pairSymbol        trading pair symbol (e.g. "BTC-USD", "ETH-EUR")
 * @param baseSymbol        base asset symbol (e.g. "BTC", "ETH")
 * @param assetName         display name (e.g. "Bitcoin", "Ethereum")
 * @param price             approximate price in quote currency
 * @param quoteCurrencyCode quote currency (e.g. "USD", "EUR")
 * @param marketCap         approximate market cap in quote currency
 * @param network           blockchain network (e.g. "Bitcoin", "Ethereum", "Solana")
 */
public record CryptoAssetPick(
        String pairSymbol,
        String baseSymbol,
        String assetName,
        double price,
        String quoteCurrencyCode,
        long marketCap,
        String network
) {
}
