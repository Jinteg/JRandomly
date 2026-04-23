package de.jinteg.randomly.internal.catalog;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Resolves deterministic entries from locale-specific property catalogs.
 */
public final class CatalogEntryLookup {

  private CatalogEntryLookup() {
    // utility class
  }

  /**
   * Returns the first catalog entry matching the given predicate.
   *
   * @param catalogPath  base catalog path without locale suffix
   * @param locale       locale used for catalog selection
   * @param matcher      entry matcher
   * @param errorMessage error message used when no match is found
   * @return raw catalog entry value
   */
  public static String firstMatchingEntry(
      String catalogPath,
      Locale locale,
      Predicate<String> matcher,
      String errorMessage
  ) {
    Objects.requireNonNull(catalogPath, "catalogPath");
    Objects.requireNonNull(locale, "locale");
    Objects.requireNonNull(matcher, "matcher");
    Objects.requireNonNull(errorMessage, "errorMessage");

    List<String> entries = NumberedPropertiesCatalog.loadList(catalogPath, locale);

    return entries.stream()
        .filter(matcher)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(errorMessage));
  }
}