package de.jinteg.randomly.internal;

import de.jinteg.randomly.JRandomly;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ReplayFileWriterTest {

    private static final Path REPLAY_FILE = Path.of("target", "jrandomly-replay.txt");

    @BeforeEach
    void setup() throws IOException {
        ReplayFileWriter.resetForTesting();
        Files.deleteIfExists(REPLAY_FILE);
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("jrandomly.seed");
        System.clearProperty("jrandomly.runStartTime");
        System.clearProperty("jrandomly.locale");
    }

    @Test
    void replayFile_isCreatedOnFirstInstance() throws IOException {
        System.setProperty("jrandomly.seed", "42");
        System.setProperty("jrandomly.runStartTime", "2026-01-01T00:00:00Z");

        JRandomly.randomly("FileTest#first");

        assertThat(REPLAY_FILE).exists();
        String content = Files.readString(REPLAY_FILE);
        assertThat(content)
                .contains("# JRandomly (Version: " + JRandomlyVersion.current() + ")")
                .contains("Replay Info")
                .contains("-Djrandomly.seed=42")
                .contains("-Djrandomly.maybeRate=0.")
                .contains("scoped(\"FileTest#first\")");
    }

    @Test
    void replayFile_appendsMultipleEntries() throws IOException {
        System.setProperty("jrandomly.seed", "99");
        System.setProperty("jrandomly.runStartTime", "2026-06-15T10:00:00Z");

        JRandomly.randomly("FileTest#a");
        JRandomly.randomly("FileTest#b");

        String content = Files.readString(REPLAY_FILE);
        assertThat(content)
                .contains("FileTest#a")
                .contains("FileTest#b");
    }

    @Test
    void replayFile_isTruncatedOnNewJvmRun() throws IOException {
        System.setProperty("jrandomly.seed", "111");
        System.setProperty("jrandomly.runStartTime", "2026-03-01T00:00:00Z");

        // Simulate first "JVM run"
        JRandomly.randomly("FileTest#old");
        String firstContent = Files.readString(REPLAY_FILE);
        assertThat(firstContent).contains("FileTest#old");

        // Simulate new "JVM run" by resetting
        ReplayFileWriter.resetForTesting();
        JRandomly.randomly("FileTest#new");

        String secondContent = Files.readString(REPLAY_FILE);
        assertThat(secondContent)
                .contains("FileTest#new")
                .doesNotContain("FileTest#old");
    }
}