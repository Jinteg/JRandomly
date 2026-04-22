package de.jinteg.randomly.domain.person;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class GenderRegistryTest {

  @Test
  void loads_german_gender_registry() {
    GenderRegistry registry = GenderRegistry.load(Locale.GERMANY);

    GenderPick male = registry.get("M");

    assertThat(registry).isNotNull();
    assertThat(male).isNotNull();
    assertThat(male.code()).isEqualTo("M");
    assertThat(male.shortLabel()).isEqualTo("M");
    assertThat(male.longLabel()).isEqualTo("Männlich");
    assertThat(registry.all()).hasSize(5);
  }

  @Test
  void loads_english_gender_registry() {
    GenderRegistry registry = GenderRegistry.load(Locale.US);

    GenderPick female = registry.get("F");

    assertThat(registry).isNotNull();
    assertThat(female).isNotNull();
    assertThat(female.code()).isEqualTo("F");
    assertThat(female.shortLabel()).isEqualTo("F");
    assertThat(female.longLabel()).isEqualTo("Female");
    assertThat(registry.all()).hasSize(5);
  }

  @Test
  void loads_turkish_gender_registry() {
    GenderRegistry registry = GenderRegistry.load(Locale.forLanguageTag("tr"));

    GenderPick male = registry.get("M");

    assertThat(registry).isNotNull();
    assertThat(male).isNotNull();
    assertThat(male.code()).isEqualTo("M");
    assertThat(male.shortLabel()).isEqualTo("E");
    assertThat(male.longLabel()).isEqualTo("Erkek");
    assertThat(registry.all()).hasSize(2);
  }

  @Test
  void loads_japanese_gender_registry() {
    GenderRegistry registry = GenderRegistry.load(Locale.forLanguageTag("ja"));

    GenderPick female = registry.get("F");

    assertThat(registry).isNotNull();
    assertThat(female).isNotNull();
    assertThat(female.code()).isEqualTo("F");
    assertThat(female.shortLabel()).isEqualTo("女");
    assertThat(female.longLabel()).isEqualTo("女性");
    assertThat(registry.all()).hasSize(2);
  }

  @Test
  void returns_null_for_unknown_code() {
    GenderRegistry registry = GenderRegistry.load(Locale.GERMANY);

    assertThat(registry.get("Z")).isNull();
  }
}