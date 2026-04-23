package de.jinteg.randomly.internal.catalog;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumberedCatalogLookupTest {

  private static final String PERSON_CATALOG_PATH =
      "de/jinteg/randomly/catalog/person/person";

  @Test
  void returns_exact_entry_by_id() {
    List<String> entries = NumberedPropertiesCatalog.loadList(PERSON_CATALOG_PATH, Locale.US);

    String firstEntry = NumberedCatalogLookup.entryById(
        PERSON_CATALOG_PATH,
        Locale.US,
        1,
        "persons"
    );

    String secondEntry = NumberedCatalogLookup.entryById(
        PERSON_CATALOG_PATH,
        Locale.US,
        2,
        "persons"
    );

    assertThat(firstEntry).isEqualTo(entries.get(0));
    assertThat(secondEntry).isEqualTo(entries.get(1))
        .isNotEqualTo(firstEntry);
  }

  @Test
  void rejects_non_positive_entry_id() {
    assertThatThrownBy(() -> NumberedCatalogLookup.entryById(
        PERSON_CATALOG_PATH,
        Locale.GERMANY,
        0,
        "persons"
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Catalog entry id 0 must be greater than 0")
        .hasMessageContaining("persons")
        .hasMessageContaining("de");
  }

  @Test
  void rejects_unknown_entry_id() {
    List<String> entries = NumberedPropertiesCatalog.loadList(PERSON_CATALOG_PATH, Locale.GERMANY);

    assertThatThrownBy(() -> NumberedCatalogLookup.entryById(
        PERSON_CATALOG_PATH,
        Locale.GERMANY,
        999,
        "persons"
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Catalog entry id 999 not found")
        .hasMessageContaining("persons")
        .hasMessageContaining("de")
        .hasMessageContaining("Allowed entry ids are 1 to " + entries.size());
  }

  @Test
  void propagates_missing_catalog_error_for_unsupported_locale() {
    assertThatThrownBy(() -> NumberedCatalogLookup.entryById(
        PERSON_CATALOG_PATH,
        Locale.ITALY,
        1,
        "persons"
    ))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Catalog not found")
        .hasMessageContaining("it");
  }
}