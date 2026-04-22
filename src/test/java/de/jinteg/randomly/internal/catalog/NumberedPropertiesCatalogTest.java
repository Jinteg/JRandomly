package de.jinteg.randomly.internal.catalog;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class NumberedPropertiesCatalogTest {

  @Test
  void loadList_reads_utf8_properties_correctly() {
    List<String> entries = NumberedPropertiesCatalog.loadList(
        "de/jinteg/randomly/catalog/person/person",
        Locale.forLanguageTag("tr")
    );

    assertThat(entries)
        .anyMatch(entry -> entry.contains("Türkiye"))
        .noneMatch(entry -> entry.contains("TÃ¼rkiye"));
  }
}