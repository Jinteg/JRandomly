package de.jinteg.randomly.domain.finance;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads static test FX rates (vs USD) from a properties file.
 * Rates are approximate and intended for test data only.
 */
final class SampleFxRates {

    private static final String FX_RATES_PATH =
            "de/jinteg/randomly/catalog/finance/fx_sample_rates.properties";

    private static final Map<String, Double> RATES = loadRates();

    private SampleFxRates() {
    }

    /**
     * Returns the test FX rate for the given currency code vs USD.
     * Returns 1.0 for USD itself.
     *
     * @param currencyCode ISO 4217 currency code
     * @return approximate rate (units of currencyCode per 1 USD)
     */
    static double rateForCurrency(String currencyCode) {
        if ("USD".equalsIgnoreCase(currencyCode)) {
            return 1.0;
        }
        Double rate = RATES.get(currencyCode.toUpperCase());
        if (rate == null) {
            throw new IllegalArgumentException(
                    "No test FX rate available for currency: " + currencyCode
                            + ". Available: USD, " + String.join(", ", RATES.keySet()));
        }
        return rate;
    }

    private static Map<String, Double> loadRates() {
        Properties props = new Properties();
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(FX_RATES_PATH)) {
            if (is == null) {
                throw new IllegalStateException("FX rates file not found: " + FX_RATES_PATH);
            }
            props.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load FX rates", e);
        }

        Map<String, Double> map = new ConcurrentHashMap<>();
        for (String key : props.stringPropertyNames()) {
            map.put(key.toUpperCase(), Double.parseDouble(props.getProperty(key).trim()));
        }
        return Map.copyOf(map);
    }
}