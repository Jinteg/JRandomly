package de.jinteg.randomly;

import de.jinteg.randomly.domain.company.CompanyPick;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class JRandomlyCompanyTest {

  @Test
  void company_api_returns_company_data() {
    CompanyPick pick = JRandomly.randomly("JRandomlyCompanyTest#data")
        .company()
        .data(Locale.US);

    assertThat(pick).isNotNull();
    assertThat(pick.name()).isNotBlank();
    assertThat(pick.countryCode()).isEqualTo("US");
    assertThat(pick.website()).isNotBlank();
  }

  @Test
  void company_api_uses_builder_locale() {
    JRandomly randomly = JRandomly.builder()
        .withScope("JRandomlyCompanyTest#locale")
        .withLocale(Locale.GERMANY)
        .build();

    CompanyPick pick = randomly.company().data();

    assertThat(pick).isNotNull();
    assertThat(pick.countryCode()).isEqualTo("DE");
    assertThat(pick.country()).isEqualTo("Deutschland");
  }

  @Test
  void company_api_supports_lookup_by_name() {
    CompanyPick pick = JRandomly.builder()
        .withScope("JRandomlyCompanyTest#lookup")
        .withLocale(Locale.US)
        .build()
        .company()
        .companyByName("Apple");

    assertThat(pick).isNotNull();
    assertThat(pick.name()).isEqualTo("Apple");
    assertThat(pick.countryCode()).isEqualTo("US");
  }
}