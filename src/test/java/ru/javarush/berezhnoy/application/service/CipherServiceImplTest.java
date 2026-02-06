package ru.javarush.berezhnoy.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.javarush.berezhnoy.domain.exception.CaesarCipherException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CipherServiceImplTest {
    @TempDir
    Path tempDir;

    private CipherServiceImpl service;
    private Path inputFile;
    private Path encryptedFile;
    private Path decryptedFile;

    @BeforeEach
    void setUp() throws IOException {
        service = new CipherServiceImpl();

        inputFile = tempDir.resolve("input.txt");
        encryptedFile = tempDir.resolve("encrypted.txt");
        decryptedFile = tempDir.resolve("decrypted.txt");

        String originalText = "Привет, мир! Это тестовый текст.";
        Files.writeString(inputFile, originalText);
    }

    @Test
    void testEncryptDecryptRoundTrip() throws Exception {
        service.encrypt(inputFile.toString(), encryptedFile.toString(), 3);
        assertTrue(Files.exists(encryptedFile));

        String encryptedText = Files.readString(encryptedFile);
        assertNotEquals(Files.readString(inputFile), encryptedText);

        service.decrypt(encryptedFile.toString(), decryptedFile.toString(), 3);
        assertTrue(Files.exists(decryptedFile));

        String decryptedText = Files.readString(decryptedFile);
        assertTrue(decryptedText.contains("Привет"));
        assertTrue(decryptedText.contains("мир"));
        assertTrue(decryptedText.contains("тестовый"));
    }

    @Test
    void testEncryptDecryptWithKeyZero() throws Exception {
        service.encrypt(inputFile.toString(), encryptedFile.toString(), 0);
        service.decrypt(encryptedFile.toString(), decryptedFile.toString(), 0);

        String original = Files.readString(inputFile);
        String result = Files.readString(decryptedFile);

        assertEquals(original, result);
    }

    @Test
    void testInvalidKeyWithExistingFile() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "Тестовый текст");
        Path outputFile = tempDir.resolve("output.txt");

        assertDoesNotThrow(() -> {
            service.encrypt(testFile.toString(), outputFile.toString(), 999);
        });

        assertTrue(Files.exists(outputFile));
    }

    @Test
    void testEncryptNonExistentFile() {
        Path nonExistent = tempDir.resolve("nonexistent.txt");
        Path output = tempDir.resolve("output.txt");

        assertThrows(CaesarCipherException.class, () -> {
            service.encrypt(nonExistent.toString(), output.toString(), 5);
        });
    }

    @Test
    void testEncryptWithNegativeKey() throws Exception {
        service.encrypt(inputFile.toString(), encryptedFile.toString(), -3);
        service.decrypt(encryptedFile.toString(), decryptedFile.toString(), -3);

        String original = Files.readString(inputFile);
        String result = Files.readString(decryptedFile);

        assertEquals(original, result);
    }
}