package de.jinteg.randomly.internal;

import de.jinteg.randomly.JRandomly;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        System.clearProperty(ReplayFileWriter.PROP_REPLAY_FILE);
        // Later tests in the same JVM must resolve the default location again
        ReplayFileWriter.resetForTesting();
    }

    @Test
    @DisplayName("Replay file location can be configured")
    void replayFile_usesConfiguredPath(@TempDir Path tempDir) throws IOException {
        Path customFile = tempDir.resolve("nested/dir/replay.txt");
        System.setProperty(ReplayFileWriter.PROP_REPLAY_FILE, customFile.toString());

        JRandomly.randomly("FileTest#custom");

        assertThat(customFile).exists();
        assertThat(Files.readString(customFile)).contains("FileTest#custom");
        assertThat(REPLAY_FILE).doesNotExist();
    }

    @ParameterizedTest
    @ValueSource(strings = {"off", "OFF", "false", "none", " off "})
    @DisplayName("Replay file can be disabled")
    void replayFile_canBeDisabled(String value) {
        System.setProperty(ReplayFileWriter.PROP_REPLAY_FILE, value);

        JRandomly.randomly("FileTest#off");

        assertThat(REPLAY_FILE).doesNotExist();
    }

    @Test
    @DisplayName("An unusable replay file location never fails the test run")
    void replayFile_unusableLocationDoesNotFail(@TempDir Path tempDir) {
        // A directory cannot be written as a file
        System.setProperty(ReplayFileWriter.PROP_REPLAY_FILE, tempDir.toString());

        JRandomly r1 = JRandomly.randomly("FileTest#unusable1");
        JRandomly r2 = JRandomly.randomly("FileTest#unusable2");

        assertThat(r1.intBetween(1, 10)).isBetween(1, 10);
        assertThat(r2.intBetween(1, 10)).isBetween(1, 10);
    }

    @Test
    @DisplayName("Many parallel instances write complete, non-interleaved lines")
    void replayFile_parallelEntriesDoNotInterleave() throws Exception {
        int threads = 8;
        int instancesPerThread = 50;
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                int thread = t;
                futures.add(executor.submit(() -> {
                    for (int i = 0; i < instancesPerThread; i++) {
                        JRandomly.randomly("FileTest#parallel-" + thread + "-" + i);
                    }
                }));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        }

        List<String> entries = Files.readAllLines(REPLAY_FILE).stream()
                .filter(line -> line.contains("FileTest#parallel-"))
                .toList();
        assertThat(entries)
                .hasSize(threads * instancesPerThread)
                .allMatch(line -> line.matches(
                        "^\\S+ \\| scoped\\(\"FileTest#parallel-\\d+-\\d+\"\\) \\| -Djrandomly\\.seed=.*"
                                + "-Djrandomly\\.version=\\S+$"));
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
    @DisplayName("Header names the caller outside of JRandomly, also when using the builder")
    void replayFile_headerNamesInitialCaller() throws IOException {
        JRandomly.builder().withScope("FileTest#caller").build();

        assertThat(Files.readString(REPLAY_FILE))
                .contains("# Initial caller: " + ReplayFileWriterTest.class.getName()
                        + "#replayFile_headerNamesInitialCaller");
    }

    @Test
    @DisplayName("Concurrent first instances write exactly one header")
    void replayFile_concurrentFirstInstancesWriteOneHeader() throws Exception {
        int threads = 8;
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            List<Future<JRandomly>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                String scope = "FileTest#parallel" + i;
                futures.add(executor.submit(() -> {
                    start.await();
                    return JRandomly.randomly(scope);
                }));
            }
            start.countDown();
            for (Future<JRandomly> future : futures) {
                future.get();
            }
        }

        List<String> lines = Files.readAllLines(REPLAY_FILE);
        assertThat(lines)
                .filteredOn(line -> line.startsWith("# Initial caller: "))
                .singleElement()
                .asString()
                .contains(ReplayFileWriterTest.class.getName());
        assertThat(lines)
                .filteredOn(line -> line.contains("FileTest#parallel"))
                .hasSize(threads);
    }

    @Test
    @DisplayName("Forks do not write their own replay entries")
    void replayFile_forksWriteNoEntries() throws IOException {
        JRandomly r = JRandomly.randomly("FileTest#forkParent");
        r.fork("a");
        r.fork("b").fork("c");

        assertThat(Files.readAllLines(REPLAY_FILE))
                .filteredOn(line -> line.contains("FileTest#forkParent"))
                .hasSize(1);
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