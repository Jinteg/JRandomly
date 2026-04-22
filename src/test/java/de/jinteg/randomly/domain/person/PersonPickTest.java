package de.jinteg.randomly.domain.person;

import de.jinteg.randomly.JRandomly;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Creates locale-specific tests for supported person catalogs.
 */
class PersonPickTest {

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
}