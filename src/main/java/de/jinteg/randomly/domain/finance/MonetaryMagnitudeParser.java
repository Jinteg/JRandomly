package de.jinteg.randomly.domain.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Parses human-readable market cap values like "2.18T", "68.3B", "125M" or plain numbers.
 */
final class MonetaryMagnitudeParser {

    private MonetaryMagnitudeParser() {
    }

    /**
     * Parse a market cap text into an absolute long value.
     *
     * @param value input value
     * @return parsed long market cap
     */
    static long parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("marketCap must not be null or blank");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT).replace(',', '.');
        long multiplier = 1L;
        String numericPart = normalized;

        char last = normalized.charAt(normalized.length() - 1);
        if (Character.isLetter(last)) {
            multiplier = switch (last) {
                case 'T' -> 1_000_000_000_000L;
                case 'B' -> 1_000_000_000L;
                case 'M' -> 1_000_000L;
                default -> throw new IllegalArgumentException(
                        "Unsupported marketCap suffix '" + last + "' in value: " + value);
            };
            numericPart = normalized.substring(0, normalized.length() - 1).trim();
        }

        try {
            return new BigDecimal(numericPart)
                    .multiply(BigDecimal.valueOf(multiplier))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact();
        } catch (NumberFormatException | ArithmeticException ex) {
            throw new IllegalArgumentException("Invalid marketCap value: " + value, ex);
        }
    }
}
