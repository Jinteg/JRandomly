package de.jinteg.randomly.internal;

/**
 * Internal numeric bounds for decimal digit-based generation.
 * <p>
 * This class is internal and not part of the public API.
 */
public final class NumericDigitBounds {

    public static final String DIGITS_PARAM = "digits";
    public static final String MAX_DIGITS_PARAM = "maxDigits";
    private static final int[] INT_POW10 = {
            1, 10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000, 1_000_000_000
    };

    private static final long[] LONG_POW10 = {
            1L,
            10L,
            100L,
            1_000L,
            10_000L,
            100_000L,
            1_000_000L,
            10_000_000L,
            100_000_000L,
            1_000_000_000L,
            10_000_000_000L,
            100_000_000_000L,
            1_000_000_000_000L,
            10_000_000_000_000L,
            100_000_000_000_000L,
            1_000_000_000_000_000L,
            10_000_000_000_000_000L,
            100_000_000_000_000_000L,
            1_000_000_000_000_000_000L
    };

    private NumericDigitBounds() {
        // no instances
    }

    /**
     * Lower bound for integer values with a given number of digits.
     *
     * @param digits       number of digits
     * @param positiveOnly whether to include only positive values
     * @return lower bound
     */
    public static int intLowerExact(int digits, boolean positiveOnly) {
        validateIntDigits(digits, DIGITS_PARAM);
        if (digits == 1) {
            return positiveOnly ? 1 : 0;
        }
        return INT_POW10[digits - 1];
    }

    /**
     * Upper bound for integer values with a given number of digits.
     *
     * @param digits number of digits
     * @return upper bound
     */
    public static int intUpperExact(int digits) {
        validateIntDigits(digits, DIGITS_PARAM);
        return digits == 10 ? Integer.MAX_VALUE : INT_POW10[digits] - 1;
    }

    /**
     * Upper bound for integer values with a given maximum number of digits.
     *
     * @param maxDigits maximum number of digits
     * @return upper bound
     */
    public static int intUpperMax(int maxDigits) {
        validateIntDigits(maxDigits, MAX_DIGITS_PARAM);
        return maxDigits == 10 ? Integer.MAX_VALUE : INT_POW10[maxDigits] - 1;
    }

    /**
     * Lower bound for long values with a given number of digits.
     *
     * @param digits       number of digits
     * @param positiveOnly whether to include only positive values
     * @return lower bound
     */
    public static long longLowerExact(int digits, boolean positiveOnly) {
        validateLongDigits(digits, DIGITS_PARAM);
        if (digits == 1) {
            return positiveOnly ? 1L : 0L;
        }
        return LONG_POW10[digits - 1];
    }

    /**
     * Upper bound for long values with a given number of digits.
     *
     * @param digits number of digits
     * @return upper bound
     */
    public static long longUpperExact(int digits) {
        validateLongDigits(digits, DIGITS_PARAM);
        return digits == 19 ? Long.MAX_VALUE : LONG_POW10[digits] - 1L;
    }

    /**
     * Upper bound for long values with a given maximum number of digits.
     *
     * @param maxDigits maximum number of digits
     * @return upper bound
     */
    public static long longUpperMax(int maxDigits) {
        validateLongDigits(maxDigits, MAX_DIGITS_PARAM);
        return maxDigits == 19 ? Long.MAX_VALUE : LONG_POW10[maxDigits] - 1L;
    }

    private static void validateIntDigits(int value, String paramName) {
        if (value < 1 || value > 10) {
            throw new IllegalArgumentException(paramName + " must be between 1 and 10");
        }
    }

    private static void validateLongDigits(int value, String paramName) {
        if (value < 1 || value > 19) {
            throw new IllegalArgumentException(paramName + " must be between 1 and 19");
        }
    }
}
