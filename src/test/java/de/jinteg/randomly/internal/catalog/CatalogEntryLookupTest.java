package de.jinteg.randomly.internal.catalog;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogEntryLookupTest {

  private static final String STOCK_CATALOG_PATH = "de/jinteg/randomly/catalog/finance/stocks";

  @Test
  void returns_first_matching_entry() {
    List<String> entries = NumberedPropertiesCatalog.loadList(STOCK_CATALOG_PATH, Locale.US);

    String entry = CatalogEntryLookup.firstMatchingEntry(
        STOCK_CATALOG_PATH,
        Locale.US,
        raw -> raw.startsWith("BRK.B|"),
        "Stock symbol BRK.B not found for locale en"
    );

    assertThat(entry).isIn(entries)
        .startsWith("BRK.B");
  }

  @Test
  void rejects_missing_match() {
    assertThatThrownBy(() -> CatalogEntryLookup.firstMatchingEntry(
        STOCK_CATALOG_PATH,
        Locale.US,
        raw -> raw.startsWith("DOES_NOT_EXIST|"),
        "Stock symbol DOES_NOT_EXIST not found for locale en"
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Stock symbol DOES_NOT_EXIST not found for locale en");
  }

  @Test
  void propagates_missing_catalog_error_for_unsupported_locale() {
    assertThatThrownBy(() -> CatalogEntryLookup.firstMatchingEntry(
        STOCK_CATALOG_PATH,
        Locale.ITALY,
        raw -> raw.startsWith("BRK.B|"),
        "Stock symbol BRK.B not found for locale it"
    ))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Catalog not found")
        .hasMessageContaining("it");
  }
}