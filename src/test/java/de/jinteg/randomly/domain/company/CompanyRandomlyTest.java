package de.jinteg.randomly.domain.company;

import de.jinteg.randomly.JRandomly;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompanyRandomlyTest {

  @Test
  @DisplayName("data() uses the configured locale")
  void data_returns_german_company_for_configured_locale() {
    CompanyPick pick = JRandomly.builder()
        .withScope("CompanyTest#de")
        .withLocale(Locale.GERMAN)
        .build()
        .company()
        .data();

    assertThat(pick).isNotNull();
    assertThat(pick.countryCode()).isEqualTo("DE");
    assertThat(pick.country()).isEqualTo("Deutschland");
    assertThat(pick.website()).isNotBlank();
    assertThat(pick.email()).isNotBlank();
    assertThat(pick.phone()).startsWith("+49");
  }

  @Test
  @DisplayName("data(Locale) uses the given locale")
  void data_returns_us_company_for_explicit_locale() {
    CompanyPick pick = JRandomly.randomly("CompanyTest#us")
        .company()
        .data(Locale.US);

    assertThat(pick).isNotNull();
    assertThat(pick.countryCode()).isEqualTo("US");
    assertThat(pick.country()).isEqualTo("United States");
    assertThat(pick.website()).isNotBlank();
    assertThat(pick.email()).isNotBlank();
    assertThat(pick.phone()).startsWith("+1");
  }

  @Test
  @DisplayName("companyByName finds a US company")
  void companyByName_returns_exact_us_company() {
    CompanyPick pick = JRandomly.randomly("CompanyTest#apple")
        .company()
        .companyByName("Apple", Locale.US);

    assertThat(pick).isNotNull();
    assertThat(pick.name()).isEqualTo("Apple");
    assertThat(pick.countryCode()).isEqualTo("US");
    assertThat(pick.website()).isNotBlank();
    assertThat(pick.email()).isNotBlank();
  }

  @Test
  @DisplayName("companyByName finds a German company")
  void companyByName_returns_exact_german_company() {
    CompanyPick pick = JRandomly.randomly("CompanyTest#sap")
        .company()
        .companyByName("SAP", Locale.GERMANY);

    assertThat(pick).isNotNull();
    assertThat(pick.name()).isEqualTo("SAP");
    assertThat(pick.countryCode()).isEqualTo("DE");
    assertThat(pick.website()).isNotBlank();
    assertThat(pick.email()).isNotBlank();
  }

  @Test
  @DisplayName("companyByName rejects an unknown name")
  void companyByName_rejects_unknown_name() {
    assertThatThrownBy(() -> JRandomly.randomly("CompanyTest#missing")
        .company()
        .companyByName("DOES_NOT_EXIST", Locale.US))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Company name DOES_NOT_EXIST not found")
        .hasMessageContaining("en");
  }
}