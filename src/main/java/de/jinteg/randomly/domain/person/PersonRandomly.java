package de.jinteg.randomly.domain.person;

import de.jinteg.randomly.JRandomly;
import de.jinteg.randomly.internal.catalog.NumberedPropertiesCatalog;
import de.jinteg.randomly.internal.catalog.RawParserUtil;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Provides person-related random data.
 */
public final class PersonRandomly {

  private static final String PERSON_CATALOG_PATH = "de/jinteg/randomly/catalog/person/person";

  private final JRandomly randomly;

  /**
   * Creates a new person randomizer.
   *
   * @param randomly random source
   */
  public PersonRandomly(JRandomly randomly) {
    this.randomly = Objects.requireNonNull(randomly, "randomly must not be null");
  }

  /**
   * Returns a random person using the configured locale.
   *
   * @return random person data
   */
  public PersonPick data() {
    return data(randomly.getLocale());
  }

  /**
   * Returns a random person for the given locale.
   *
   * @param locale locale used for catalog selection
   * @return random person data
   */
  public PersonPick data(Locale locale) {
    Objects.requireNonNull(locale, "locale");
    List<String> entries = NumberedPropertiesCatalog.loadList(PERSON_CATALOG_PATH, locale);
    String raw = entries.get(randomly.index(entries.size()));
    return PersonParser.parse(RawParserUtil.parse(raw, PersonParser.COLUMN_COUNT));
  }

  /**
   * Returns a random gender using the configured locale.
   *
   * @return random gender
   */
  public GenderPick gender() {
    return gender(randomly.getLocale());
  }

  /**
   * Returns a random gender for the given locale.
   *
   * @param locale locale used for catalog selection
   * @return random gender
   */
  public GenderPick gender(Locale locale) {
    Objects.requireNonNull(locale, "locale");

    GenderRegistry genderRegistry = GenderRegistry.load(locale);
    List<GenderPick> genders = List.copyOf(genderRegistry.all());

    return genders.get(randomly.index(genders.size()));
  }
}