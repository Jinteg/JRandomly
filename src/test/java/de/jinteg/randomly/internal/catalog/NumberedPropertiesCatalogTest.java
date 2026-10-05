package de.jinteg.randomly.internal.catalog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

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

  @Test
  @DisplayName("Keeps entries of all key prefixes in a catalog with several groups")
  void loadList_keeps_entries_of_all_key_prefixes() throws IOException {
    for (Locale locale : List.of(Locale.GERMAN, Locale.ENGLISH)) {
      String basePath = "de/jinteg/randomly/catalog/finance/stocks";

      List<String> entries = NumberedPropertiesCatalog.loadList(basePath, locale);

      assertThat(entries)
          .as("all numbered entries of %s_%s", basePath, locale.getLanguage())
          .hasSize(countNumberedEntries(basePath + "_" + locale.getLanguage() + ".properties"));
    }
  }

  @Test
  @DisplayName("Orders entries by key prefix, then by number")
  void loadList_orders_by_prefix_then_number() {
    List<String> entries = NumberedPropertiesCatalog.loadList(
        "de/jinteg/randomly/catalog/finance/stocks",
        Locale.GERMAN
    );

    assertThat(entries.getFirst()).startsWith("SAP|");
  }

  @Test
  @DisplayName("Ignores a UTF-8 byte order mark in front of the first key")
  void loadList_ignores_byte_order_mark() {
    List<String> entries = NumberedPropertiesCatalog.loadList(
        "de/jinteg/randomly/catalog/text/adjectives/adjectives",
        Locale.GERMAN
    );

    assertThat(entries.getFirst()).isEqualTo("alt");
  }

  private static int countNumberedEntries(String resource) throws IOException {
    try (InputStream in = NumberedPropertiesCatalogTest.class.getClassLoader()
        .getResourceAsStream(resource)) {
      assertThat(in).as("resource %s", resource).isNotNull();
      Properties properties = new Properties();
      properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
      return (int) properties.stringPropertyNames().stream()
          .filter(key -> key.matches(".+\\.\\d+"))
          .count();
    }
  }
}