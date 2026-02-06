package ru.javarush.berezhnoy.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.javarush.berezhnoy.domain.model.CaesarCipher;
import ru.javarush.berezhnoy.domain.model.EnhancedAlphabet;
import ru.javarush.berezhnoy.domain.port.FileProcessor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BruteForceDecryptorTest {

    private CaesarCipher cipher;
    private BruteForceDecryptor decryptor;

    @BeforeEach
    void setUp() {
        EnhancedAlphabet alphabet = EnhancedAlphabetFromTestConfig.russian();
        FileProcessor noOpFileProcessor = (pathIn, pathOut, charProcessor) -> { };
        cipher = new CaesarCipher(alphabet, noOpFileProcessor);
        decryptor = new BruteForceDecryptor(cipher);
    }

    @Test
    void findKey_encryptedWithKnownKey_returnsThatKey() throws Exception {
        String plainText = "Привет мир это тест для проверки брутфорса.";
        int expectedKey = 5;

        String encrypted = cipher.encryptString(plainText, expectedKey);
        int foundKey = decryptor.findKey(encrypted);

        assertEquals(expectedKey, foundKey);
    }

    /**
     * Test helper: Russian alphabet matching application config (letters + space + basic punctuation).
     */
    private static final class EnhancedAlphabetFromTestConfig {
        static EnhancedAlphabet russian() {
            String letters = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
            String symbols = letters + " .,!?:;-";
            return new EnhancedAlphabet(symbols.toCharArray());
        }
    }
}
