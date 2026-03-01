package de.jinteg.randomly.core;

import de.jinteg.randomly.JRandomly;
import de.jinteg.randomly.internal.NumericDigitBounds;

import java.util.Objects;
import java.util.UUID;

/**
 * Deterministic generators for identifiers (UUIDs, numeric IDs).
 * <p>
 * All values are derived from the parent {@link JRandomly} instance's RNG stream —
 * never from {@link UUID#randomUUID()} or other external entropy sources.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * JRandomly r = JRandomly.randomly("MyTest");
 * UUID   orderId  = r.id().uuid();
 * long   itemId   = r.id().longId();
 * long   seqNr    = r.id().longIdBetween(1_000, 9_999);
 * String ref      = r.id().prefixedId("ORD-", 12);
 * }</pre>
 */
public final class IdRandomly {

    private final JRandomly randomly;

    /**
     * Constructor.
     *
     * @param randomly random number generator
     */
    public IdRandomly(JRandomly randomly) {
        this.randomly = Objects.requireNonNull(randomly, "randomly must not be null");
    }

    // ---- UUID ----

    /**
     * Returns a deterministic Version-4 UUID built entirely from the RNG stream.
     * <p>
     * The UUID has the correct variant (2) and version (4) bits set,
     * so it is structurally valid — but reproducible given the same seed.
     *
     * @return UUID
     */
    public UUID uuid() {
        // 32 hex chars = 128 random bits
        String hex = randomly.text().hexString(32);

        // Inject version 4: hex[12] = '4'
        // Inject variant 2: hex[16] must be 8, 9, a, or b
        char[] chars = hex.toCharArray();
        chars[12] = '4';
        chars[16] = variantChar(chars[16]);

        // Format: 8-4-4-4-12
        return UUID.fromString(
                new String(chars, 0, 8) + "-"
                        + new String(chars, 8, 4) + "-"
                        + new String(chars, 12, 4) + "-"
                        + new String(chars, 16, 4) + "-"
                        + new String(chars, 20, 12)
        );
    }

    // ---- Long IDs ----

    /**
     * Returns a random positive long ID in the range [1, Long.MAX_VALUE].
     * <p>
     * This is the most common case for synthetic database-style IDs.
     *
     * @return long ID
     */
    public long longId() {
        return randomly.longBetween(1, Long.MAX_VALUE);
    }

    /**
     * Returns a random long ID in the range [min, max] (both inclusive).
     *
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @return random long ID
     * @throws IllegalArgumentException if {@code min < 1}
     */
    public long longIdBetween(long min, long max) {
        if (min < 1) {
            throw new IllegalArgumentException("min must be >= 1 (IDs should be positive)");
        }
        return randomly.longBetween(min, max);
    }

    // ---- Int IDs ----

    /**
     * Returns a random positive int ID in the range [1, Integer.MAX_VALUE].
     *
     * @return int ID
     */
    public int intId() {
        return randomly.intBetween(1, Integer.MAX_VALUE);
    }

    /**
     * Returns a random int ID in the range [min, max] (both inclusive).
     *
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @return random int ID
     * @throws IllegalArgumentException if min > max or {@code min < 1}
     */
    public int intIdBetween(int min, int max) {
        if (min < 1) {
            throw new IllegalArgumentException("min must be >= 1 (IDs should be positive)");
        }
        return randomly.intBetween(min, max);
    }

    /**
     * Returns a positive int ID with exactly {@code digits} decimal digits.
     *
     * @param digits number of digits (1 to 10)
     * @return int ID with exactly {@code digits} digits
     */
    public int intIdWithExactDigits(int digits) {
        int lowerInclusive = NumericDigitBounds.intLowerExact(digits, true);
        int upperInclusive = NumericDigitBounds.intUpperExact(digits);
        if (lowerInclusive > upperInclusive) {
            throw new IllegalArgumentException("No int ID range available for digits=" + digits);
        }
        return randomly.intBetween(lowerInclusive, upperInclusive);
    }

    /**
     * Returns a positive int ID with at most {@code maxDigits} decimal digits.
     *
     * @param maxDigits maximum number of digits (1 to 10)
     * @return int ID with at most {@code maxDigits} digits
     */
    public int intIdWithMaxDigits(int maxDigits) {
        int upperInclusive = NumericDigitBounds.intUpperMax(maxDigits);
        return randomly.intBetween(1, upperInclusive);
    }

    /**
     * Returns a positive long ID with exactly {@code digits} decimal digits.
     *
     * @param digits number of digits (1 to 19)
     * @return long ID with exactly {@code digits} digits
     */
    public long longIdWithExactDigits(int digits) {
        long lowerInclusive = NumericDigitBounds.longLowerExact(digits, true);
        long upperInclusive = NumericDigitBounds.longUpperExact(digits);
        if (lowerInclusive > upperInclusive) {
            throw new IllegalArgumentException("No long ID range available for digits=" + digits);
        }
        return randomly.longBetween(lowerInclusive, upperInclusive);
    }

    /**
     * Returns a positive long ID with at most {@code maxDigits} decimal digits.
     *
     * @param maxDigits maximum number of digits (1 to 19)
     * @return long ID with at most {@code maxDigits} digits
     */
    public long longIdWithMaxDigits(int maxDigits) {
        long upperInclusive = NumericDigitBounds.longUpperMax(maxDigits);
        return randomly.longBetween(1L, upperInclusive);
    }

    /**
     * Returns an alphanumeric key with exactly {@code length} characters.
     * <p>
     * Example: {@code key(12)} → {@code "a7Bx3kZ9Pq1R"}
     *
     * @param length key length (must be > 0)
     * @return alphanumeric key with exact length
     * @throws IllegalArgumentException if {@code length <= 0}
     */
    public String key(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be > 0");
        }
        return randomly.text().prefixedAlphaNumeric("", length, length);
    }

    /**
     * Returns a numeric key with exactly {@code length} characters.
     * <p>
     * Leading zeros are allowed.
     * Example: {@code numericKey(6)} → {@code "004271"}
     *
     * @param length key length (must be > 0)
     * @return numeric key with exact length
     * @throws IllegalArgumentException if {@code length <= 0}
     */
    public String numericKey(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be > 0");
        }
        return randomly.text().numericString(length);
    }
    // ---- String IDs ----

    /**
     * Returns a prefixed alphanumeric ID string with the given total length.
     * <p>
     * Example: {@code prefixedId("ORD-", 12)} → {@code "ORD-a7Bx3kZ9"}
     *
     * @param prefix      the prefix (e.g. "ORD-", "USR-")
     * @param totalLength total length of the resulting string (prefix + random part)
     * @return prefixed alphanumeric ID string
     * @throws IllegalArgumentException if the prefix is longer than totalLength
     */
    public String prefixedId(String prefix, int totalLength) {
        return randomly.text().prefixedAlphaNumeric(prefix, totalLength, totalLength);
    }

    // ---- Internal helpers ----

    /**
     * Maps a hex char to the variant-2 range (8, 9, a, b) for UUID compliance.
     */
    private static char variantChar(char hexChar) {
        // Take the lower 2 bits of the hex digit and OR with 0b1000
        int nibble = Character.digit(hexChar, 16);
        int variant = 0x8 | (nibble & 0x3); // result: 8, 9, a, or b
        return Character.forDigit(variant, 16);
    }
}