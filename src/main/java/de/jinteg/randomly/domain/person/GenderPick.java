package de.jinteg.randomly.domain.person;

/**
 * Gender pick with code, short label, and long label.
 *
 * @param code       1-letter code - M, F, O, X, U
 * @param shortLabel 1-letter short label, e.g. "M" for "Male", "D" for "Divers"
 * @param longLabel  long label
 */
public record GenderPick(
    String code,      // M, F, O, X, U
    String shortLabel, // M, W, D, X, U (for DE)
    String longLabel   // Male, Female, Other, ...
) {
}
