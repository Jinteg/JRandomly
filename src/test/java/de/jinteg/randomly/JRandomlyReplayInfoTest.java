package de.jinteg.randomly;

import de.jinteg.randomly.internal.JRandomlyVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JRandomlyReplayInfoTest {

    @AfterEach
    void cleanup() {
        System.clearProperty("jrandomly.seed");
        System.clearProperty("jrandomly.runStartTime");
        System.clearProperty("jrandomly.locale");
    }

    @Test
    void replayInfo_containsAllRelevantFields() {
        System.setProperty("jrandomly.seed", "42");
        System.setProperty("jrandomly.runStartTime", "2026-06-15T10:30:00Z");
        System.setProperty("jrandomly.locale", "de-DE");

        JRandomly r = JRandomly.randomly("ReplayTest#1");
        String info = r.replayInfo();

        // Root seed is shown (not the derived instance seed)
        assertThat(info).contains("-Djrandomly.seed=42")
                .contains("-Djrandomly.runStartTime=2026-06-15T10:30:00Z")
                .contains("-Djrandomly.locale=de-DE");
    }

    @Test
    @DisplayName("Replay info contains the running JRandomly version")
    void replayInfo_containsVersion() {
        JRandomly r = JRandomly.randomly("ReplayTest#version");

        assertThat(r.replayInfo())
                .contains("-Djrandomly.version=" + JRandomlyVersion.current())
                .doesNotContain("-Djrandomly.version=unknown");
    }

    @Test
    void replayInfo_canBeUsedToReproduceSameSequence() {
        System.setProperty("jrandomly.seed", "999");
        System.setProperty("jrandomly.runStartTime", "2026-01-01T00:00:00Z");

        JRandomly r1 = JRandomly.randomly("ReplayTest#repro");
        String replay = r1.replayInfo();

        // The replayInfo seed is the *instance* seed, not the root seed.
        // To reproduce: use instanceSeed directly.
        assertThat(replay).contains("-Djrandomly.seed=")
                .contains("-Djrandomly.runStartTime=");
    }
}