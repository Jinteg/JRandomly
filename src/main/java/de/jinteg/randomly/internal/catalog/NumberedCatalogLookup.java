package de.jinteg.randomly.internal.catalog;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Resolves exact numbered entries from locale-specific numbered property catalogs.
 */
public final class NumberedCatalogLookup {

  private NumberedCatalogLookup() {
    // utility class
  }

  /**
   * Returns the exact numbered catalog entry for the given id and locale.
   *
   * @param catalogPath  base catalog path without locale suffix
   * @param locale       locale used for catalog selection
   * @param entryId      1-based catalog entry id
   * @param catalogLabel human-readable catalog label for error messages
   * @return raw catalog entry value
   */
  public static String entryById(
      String catalogPath,
      Locale locale,
      int entryId,
      String catalogLabel
  ) {
    Objects.requireNonNull(catalogPath, "catalogPath");
    Objects.requireNonNull(locale, "locale");
    Objects.requireNonNull(catalogLabel, "catalogLabel");

    if (entryId <= 0) {
      throw new IllegalArgumentException(
          "Catalog entry id %s must be greater than 0 for %s and locale %s"
              .formatted(entryId, catalogLabel, locale.getLanguage())
      );
    }

    List<String> entries = NumberedPropertiesCatalog.loadList(catalogPath, locale);

    if (entryId > entries.size()) {
      throw new IllegalArgumentException(
          "Catalog entry id %s not found for %s and locale %s. Allowed entry ids are 1 to %s."
              .formatted(entryId, catalogLabel, locale.getLanguage(), entries.size())
      );
    }

    return entries.get(entryId - 1);
  }
}