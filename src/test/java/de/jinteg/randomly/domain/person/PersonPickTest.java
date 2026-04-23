package de.jinteg.randomly.domain.person;

import de.jinteg.randomly.JRandomly;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Creates locale-specific tests for supported person catalogs.
 */
class PersonPickTest {

  @AfterEach
  void cleanup() {
    System.clearProperty("jrandomly.locale");
  }

  @Test
  void picks_german_person_correctly() {
    System.setProperty("jrandomly.locale", "de");
    JRandomly randomly = JRandomly.randomly();
    List<String> genderOptions = List.of("M", "F", "O", "X", "U");

    PersonPick pick = randomly.person().data();

    assertThat(pick).isNotNull();
    assertThat(pick.languages()).contains("de-DE");
    assertThat(pick.genderCode()).isIn(genderOptions);
    assertThat(pick.birthPlace()).endsWith("Deutschland");
    assertThat(pick.nationality()).isEqualTo("Deutschland");
    assertThat(pick.mobileNumber()).startsWith("+49");
  }

  @Test
  void picks_us_person_correctly() {
    System.setProperty("jrandomly.locale", "en");
    JRandomly randomly = JRandomly.randomly();
    List<String> genderOptions = List.of("M", "F", "O", "X", "U");

    PersonPick pick = randomly.person().data();

    assertThat(pick).isNotNull();
    assertThat(pick.languages()).contains("en-US");
    assertThat(pick.genderCode()).isIn(genderOptions);
    assertThat(pick.nationality()).isEqualTo("United States");
    assertThat(pick.birthPlace()).endsWith("USA");
    assertThat(pick.mobileNumber()).startsWith("+1");
  }

  @Test
  void picks_turkish_person_correctly() {
    System.setProperty("jrandomly.locale", "tr");
    JRandomly randomly = JRandomly.randomly();
    List<String> genderOptions = List.of("M", "F");

    PersonRandomly person = randomly.person();
    PersonPick pick = person.data();

    assertThat(pick).isNotNull();
    assertThat(pick.languages()).contains("tr-TR");
    assertThat(pick.genderCode()).isIn(genderOptions);
    assertThat(pick.nationality()).isEqualTo("Türkiye");
    assertThat(pick.birthPlace()).endsWith("Türkiye");
    assertThat(pick.mobileNumber()).startsWith("+90");
  }

  @Test
  void personById_returns_exact_us_person() {
    PersonPick pick = JRandomly.randomly("PersonTest#byIdUs")
        .person()
        .personById(1, Locale.US);

    assertThat(pick).isNotNull();
    assertThat(pick.username()).isEqualTo("jdoeA12");
    assertThat(pick.name()).isEqualTo("John");
    assertThat(pick.familyName()).isEqualTo("Doe");
    assertThat(pick.genderCode()).isEqualTo("M");
    assertThat(pick.birthPlace()).isEqualTo("New York, USA");
    assertThat(pick.nationality()).isEqualTo("United States");
    assertThat(pick.languages()).isEqualTo("en-US");
  }

  @Test
  void personById_uses_configured_locale_by_default() {
    System.setProperty("jrandomly.locale", "de");

    PersonPick pick = JRandomly.randomly("PersonTest#byIdDefaultLocale")
        .person()
        .personById(1);

    assertThat(pick).isNotNull();
    assertThat(pick.username()).isEqualTo("mkellerA1");
    assertThat(pick.name()).isEqualTo("Markus");
    assertThat(pick.familyName()).isEqualTo("Keller");
    assertThat(pick.birthPlace()).isEqualTo("München, Deutschland");
    assertThat(pick.nationality()).isEqualTo("Deutschland");
    assertThat(pick.languages()).isEqualTo("de-DE");
  }

  @Test
  void personById_rejects_non_positive_id() {
    assertThatThrownBy(() -> JRandomly.randomly("PersonTest#byIdZero")
        .person()
        .personById(0, Locale.GERMANY))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Catalog entry id 0 must be greater than 0")
        .hasMessageContaining("persons")
        .hasMessageContaining("de");
  }

  @Test
  void personById_rejects_unknown_id() {
    assertThatThrownBy(() -> JRandomly.randomly("PersonTest#byIdMissing")
        .person()
        .personById(999, Locale.GERMANY))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Catalog entry id 999 not found")
        .hasMessageContaining("persons")
        .hasMessageContaining("de")
        .hasMessageContaining("Allowed entry ids are 1 to");
  }
}