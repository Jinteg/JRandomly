package de.jinteg.randomly.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JRandomlyVersionTest {

  @Test
  @DisplayName("Current version is filled in by Maven resource filtering")
  void current_version_is_filtered() {
    assertThat(JRandomlyVersion.current())
        .isNotEqualTo(JRandomlyVersion.UNKNOWN)
        .doesNotContain("${")
        .matches("\\d+\\.\\d+\\.\\d+.*");
  }

  @Test
  @DisplayName("Detects a replay recorded with a different version")
  void differs_detects_other_version() {
    assertThat(JRandomlyVersion.differs("0.2.0", "0.3.0")).isTrue();
  }

  @Test
  @DisplayName("Same version or missing replay version is no mismatch")
  void differs_ignores_same_or_missing_version() {
    assertThat(JRandomlyVersion.differs("0.3.0", "0.3.0")).isFalse();
    assertThat(JRandomlyVersion.differs(" 0.3.0 ", "0.3.0")).isFalse();
    assertThat(JRandomlyVersion.differs(null, "0.3.0")).isFalse();
    assertThat(JRandomlyVersion.differs("  ", "0.3.0")).isFalse();
  }

  @Test
  @DisplayName("Unknown current version never reports a mismatch")
  void differs_ignores_unknown_current_version() {
    assertThat(JRandomlyVersion.differs("0.2.0", JRandomlyVersion.UNKNOWN)).isFalse();
  }
}
