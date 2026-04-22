package de.jinteg.randomly.internal;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigLoaderTest {
  private static Locale defaultLocale;

  @BeforeAll
  static void setUp() {
    System.clearProperty("jrandomly.locale");
    defaultLocale = Locale.getDefault();
  }

    @Test
    @DisplayName("should load default values")
    void shouldLoadDefaultValues() {
      System.clearProperty("jrandomly.locale");
        System.setProperty("jrandomly.maybeRate", "0.125");

        ConfigLoader configLoader = new ConfigLoader();
        JRandomlyConfig config = configLoader.load();

        assertThat(config).isNotNull();
        assertThat(config.rootSeed()).isEmpty();
      assertThat(config.locale()).isEqualTo(defaultLocale);
        assertThat(config.maybeRate()).isEqualTo(0.125);
    }

    @Test
    @DisplayName("should load from system properties")
    void shouldLoadFromSystemProperties() {
        System.setProperty("jrandomly.seed", "6789");
        System.setProperty("jrandomly.locale", "it_IT");
        System.setProperty("jrandomly.maybeRate", "0.44");

        JRandomlyConfig config = new ConfigLoader().load();
        assertNotNull(config);
        assertThat(config.rootSeed()).hasValue(6789L);
        assertThat(config.locale()).isEqualTo(Locale.ITALY);
        assertThat(config.maybeRate()).isEqualTo(0.44D);
    }
}