package de.jinteg.randomly.domain.company;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompanyParserTest {

  private static final int EXPECTED_COLUMN_COUNT = 14;

  @Test
  @DisplayName("Verifies column count")
  void verifies_column_count_correctly() {
    assertThat(CompanyParser.COLUMN_COUNT).isEqualTo(EXPECTED_COLUMN_COUNT);
  }

  @Test
  @DisplayName("Parses company correctly")
  void parses_company_correctly() {
    String line =
        "Apple|Infinite Loop|1|Cupertino|95014|California|United States|US|www.apple.com|contact@apple.com|+1 408 996 1010|Technology|Consumer Electronics|US123456789";
    String[] parts = line.split("\\|");

    CompanyPick pick = CompanyParser.parse(parts);

    assertThat(pick).isNotNull();
    assertThat(pick.name()).isEqualTo("Apple");
    assertThat(pick.street()).isEqualTo("Infinite Loop");
    assertThat(pick.streetNumber()).isEqualTo("1");
    assertThat(pick.city()).isEqualTo("Cupertino");
    assertThat(pick.zipCode()).isEqualTo("95014");
    assertThat(pick.state()).isEqualTo("California");
    assertThat(pick.country()).isEqualTo("United States");
    assertThat(pick.countryCode()).isEqualTo("US");
    assertThat(pick.website()).isEqualTo("www.apple.com");
    assertThat(pick.email()).isEqualTo("contact@apple.com");
    assertThat(pick.phone()).isEqualTo("+1 408 996 1010");
    assertThat(pick.sector()).isEqualTo("Technology");
    assertThat(pick.industry()).isEqualTo("Consumer Electronics");
    assertThat(pick.vatId()).isEqualTo("US123456789");
  }

  @Test
  @DisplayName("Validators work correctly")
  void validators_work_correctly() {
    String line =
        "Apple|Infinite Loop|1|Cupertino|95014|California|United States|US|www.apple.com|INVALID-EMAIL|INVALID-PHONE|Technology|Consumer Electronics|US123456789";
    String[] parts = line.split("\\|");

    CompanyPick pick = CompanyParser.parse(parts);

    assertThat(pick).isNotNull();
    assertThat(pick.name()).isEqualTo("Apple");
    assertThat(pick.email()).isNull();
    assertThat(pick.phone()).isNull();
  }
}