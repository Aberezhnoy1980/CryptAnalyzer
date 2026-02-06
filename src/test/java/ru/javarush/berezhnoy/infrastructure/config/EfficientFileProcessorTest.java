package ru.javarush.berezhnoy.infrastructure.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.javarush.berezhnoy.domain.port.FileProcessor;
import ru.javarush.berezhnoy.infrastructure.io.EfficientFileProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EfficientFileProcessorTest {
    @TempDir
    Path tempDir;

    private Path testInputFile;
    private Path testOutputFile;
    private EfficientFileProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        testInputFile = tempDir.resolve("input.txt");
        testOutputFile = tempDir.resolve("output.txt");
        String testContent = "Hello World!\nЭто тестовый текст.\n1234567890";
        Files.write(testInputFile, testContent.getBytes(StandardCharsets.UTF_8));
        processor = new EfficientFileProcessor();
    }

    @AfterEach
    void tearDown() {
        ApplicationConfig.clearProperties();
    }

    @Test
    void testProcessFileByChunks_withIdentityProcessor() throws IOException {
        FileProcessor.CharProcessor identity = c -> c;
        processor.processFileByChunks(
                testInputFile.toString(),
                testOutputFile.toString(),
                identity
        );
        assertTrue(Files.exists(testOutputFile));
        assertEquals(Files.readString(testInputFile), Files.readString(testOutputFile));
    }

    @Test
    void testProcessFileByChunks_withUpperCaseProcessor() throws IOException {
        FileProcessor.CharProcessor toUpper = Character::toUpperCase;
        processor.processFileByChunks(
                testInputFile.toString(),
                testOutputFile.toString(),
                toUpper
        );
        assertEquals(Files.readString(testInputFile).toUpperCase(), Files.readString(testOutputFile));
    }

    @Test
    void testConstructor_withCustomSettings() {
        assertNotNull(new EfficientFileProcessor(8192, StandardCharsets.UTF_16));
    }

    @Test
    void testConstructor_withInvalidBufferSize() {
        assertThrows(IllegalArgumentException.class, () ->
                new EfficientFileProcessor(1024, StandardCharsets.UTF_8));
        assertThrows(IllegalArgumentException.class, () ->
                new EfficientFileProcessor(2048 * 1024, StandardCharsets.UTF_8));
    }

    @Test
    void testConstructor_withNullCharset() {
        assertThrows(IllegalArgumentException.class, () ->
                new EfficientFileProcessor(8192, null));
    }

    @Test
    void testProcessFileByChunks_withNonexistentInput() {
        assertThrows(IOException.class, () -> processor.processFileByChunks(
                "/nonexistent/path/input.txt",
                testOutputFile.toString(),
                c -> c
        ));
    }

    @Test
    @Disabled("Need to fix security path logic")
    void testProcessFileByChunks_withSystemProtectedPath() throws IOException {
        ApplicationConfig.setProperty("caesar.security.protected.paths", tempDir.toString().toLowerCase());
        assertThrows(SecurityException.class, () -> processor.processFileByChunks(
                testInputFile.toString(),
                testOutputFile.toString(),
                c -> c
        ));
    }
}
