package de.jinteg.randomly.domain.company;

import de.jinteg.randomly.internal.catalog.NumberedPropertiesCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Data quality checks for the company catalogs.
 *
 * <p>{@link CompanyParser} turns invalid email and phone values into {@code null}. This test
 * makes sure the shipped catalogs never rely on that: every entry must be complete and valid.
 */
class CompanyCatalogTest {

  private static final String COMPANY_CATALOG_PATH = "de/jinteg/randomly/catalog/company/company";

  @ParameterizedTest(name = "{0}")
  @CsvSource({
      "de, DE",
      "en, US",
      "ja, JP",
      "tr, TR"
  })
  @DisplayName("Every company entry has exactly 14 non-blank, valid columns")
  void every_entry_is_complete_and_valid(String language, String expectedCountryCode) {
    List<String> entries =
        NumberedPropertiesCatalog.loadList(COMPANY_CATALOG_PATH, Locale.forLanguageTag(language));

    assertThat(entries).isNotEmpty();
    for (String raw : entries) {
      String[] parts = raw.split("\\|", -1);

      assertThat(parts)
          .as("column count of %s", raw)
          .hasSize(CompanyParser.COLUMN_COUNT)
          .allSatisfy(part -> assertThat(part).as("column of %s", raw).isNotBlank());
      assertThat(raw)
          .as("only plain hyphens (no U+2011) in %s", raw)
          .doesNotContain("‑");

      CompanyPick pick = CompanyParser.parse(parts);
      assertThat(pick.email()).as("email of %s", pick.name()).isNotNull();
      assertThat(pick.phone()).as("phone of %s", pick.name()).isNotNull();
      assertThat(pick.countryCode()).as("country code of %s", pick.name())
          .isEqualTo(expectedCountryCode);
    }
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"de", "en", "ja", "tr"})
  @DisplayName("Company names are unique per catalog, so companyByName is unambiguous")
  void company_names_are_unique(String language) {
    List<String> names = NumberedPropertiesCatalog
        .loadList(COMPANY_CATALOG_PATH, Locale.forLanguageTag(language))
        .stream()
        .map(raw -> raw.substring(0, raw.indexOf('|')).trim().toLowerCase(Locale.ROOT))
        .toList();

    assertThat(names).doesNotHaveDuplicates();
  }
}
