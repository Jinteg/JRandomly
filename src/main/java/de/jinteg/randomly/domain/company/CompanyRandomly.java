package de.jinteg.randomly.domain.company;

import de.jinteg.randomly.JRandomly;
import de.jinteg.randomly.internal.catalog.CatalogEntryLookup;
import de.jinteg.randomly.internal.catalog.NumberedPropertiesCatalog;
import de.jinteg.randomly.internal.catalog.RawParserUtil;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Provides company-related random data.
 */
public final class CompanyRandomly {

  private static final String COMPANY_CATALOG_PATH = "de/jinteg/randomly/catalog/company/company";

  private final JRandomly randomly;

  /**
   * Creates a new company randomizer.
   *
   * @param randomly random source
   */
  public CompanyRandomly(JRandomly randomly) {
    this.randomly = Objects.requireNonNull(randomly, "randomly must not be null");
  }

  private static boolean hasLeadingColumn(String rawEntry, String expectedValue) {
    int separatorIndex = rawEntry.indexOf('|');
    if (separatorIndex < 0) {
      return false;
    }
    return rawEntry.substring(0, separatorIndex).trim().equalsIgnoreCase(expectedValue);
  }

  /**
   * Returns a random company using the configured locale.
   *
   * @return random company data
   */
  public CompanyPick data() {
    return data(randomly.getLocale());
  }

  /**
   * Returns a random company for the given locale.
   *
   * @param locale locale used for catalog selection
   * @return random company data
   */
  public CompanyPick data(Locale locale) {
    Objects.requireNonNull(locale, "locale");

    List<String> entries = NumberedPropertiesCatalog.loadList(COMPANY_CATALOG_PATH, locale);
    String raw = entries.get(randomly.index(entries.size()));

    return CompanyParser.parse(RawParserUtil.parse(raw, CompanyParser.COLUMN_COUNT));
  }

  /**
   * Returns the exact company entry for the given company name using the configured locale.
   *
   * @param name company name
   * @return company data
   */
  public CompanyPick companyByName(String name) {
    return companyByName(name, randomly.getLocale());
  }

  /**
   * Returns the exact company entry for the given company name and locale.
   *
   * @param name   company name
   * @param locale locale used for catalog selection
   * @return company data
   */
  public CompanyPick companyByName(String name, Locale locale) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(locale, "locale");

    String normalizedName = name.trim();

    String raw = CatalogEntryLookup.firstMatchingEntry(
        COMPANY_CATALOG_PATH,
        locale,
        entry -> hasLeadingColumn(entry, normalizedName),
        "Company name %s not found for locale %s"
            .formatted(normalizedName, locale.getLanguage())
    );

    return CompanyParser.parse(RawParserUtil.parse(raw, CompanyParser.COLUMN_COUNT));
  }
}