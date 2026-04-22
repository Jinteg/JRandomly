package de.jinteg.randomly.domain.person;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PersonParserTest {
  private static final int EXPECTED_COLUMN_COUNT = 14;


  @Test
  void verify_Columns_count_correctly() {
    // when / then
    assertThat(PersonParser.COLUMN_COUNT).isEqualTo(EXPECTED_COLUMN_COUNT);
  }


  @Test
  void validators_work_correctly() {
    // given
    String line = "USERNAME|NAME|FAMILY-NAME|Z|2001-12-21|BIRTHPLACE|OCCUPATION|NATIONALITY|+2MOBILE_NUMBER1|INVALID-EMAIL|MIDDLE-NAME|PREFERRED-NAME|English, French|MARITAL-STATUS";
    String[] parts = line.split("\\|");

    // when
    PersonPick pick = PersonParser.parse(parts);

    // then
    assertThat(pick).isNotNull();
    assertThat(pick.username()).isEqualTo("USERNAME");

    assertThat(pick.genderCode()).isEqualTo("Z");
    assertThat(pick.mobileNumber()).isNull();
    assertThat(pick.email()).isNull();
  }

  @Test
  void parses_Person_correctly() {
    // given
    String line = "USERNAME|NAME|FAMILY-NAME|M|2001-12-21|BIRTHPLACE|OCCUPATION|NATIONALITY|+1 212 555 0198|EMAIL@MAIL.US|MIDDLE-NAME|PREFERRED-NAME|English, French|MARITAL-STATUS";
    String[] parts = line.split("\\|");

    // when
    PersonPick pick = PersonParser.parse(parts);

    // then
    assertThat(pick).isNotNull();

    assertThat(pick.username()).isEqualTo("USERNAME");
    assertThat(pick.name()).isEqualTo("NAME");
    assertThat(pick.familyName()).isEqualTo("FAMILY-NAME");
    assertThat(pick.genderCode()).isEqualTo("M");
    assertThat(pick.birthday()).isEqualTo(LocalDate.of(2001, 12, 21));
    assertThat(pick.birthPlace()).isEqualTo("BIRTHPLACE");
    assertThat(pick.nationality()).isEqualTo("NATIONALITY");
    assertThat(pick.mobileNumber()).isEqualTo("+1 212 555 0198");
    assertThat(pick.email()).isEqualTo("EMAIL@MAIL.US");
    assertThat(pick.middleName()).isEqualTo("MIDDLE-NAME");
    assertThat(pick.preferredName()).isEqualTo("PREFERRED-NAME");
    assertThat(pick.languages()).isEqualTo("English, French");
    assertThat(pick.maritalStatus()).isEqualTo("MARITAL-STATUS");
  }

}