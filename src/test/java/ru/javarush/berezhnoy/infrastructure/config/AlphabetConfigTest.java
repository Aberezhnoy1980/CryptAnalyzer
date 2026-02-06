package ru.javarush.berezhnoy.infrastructure.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlphabetConfigTest {

    @BeforeEach
    @AfterEach
    void resetConfig() {
        AlphabetConfig.clearCache();
    }

    @Test
    void testGetAlphabetType() {
        assertEquals("russian", AlphabetConfig.getAlphabetType());
    }

    @Test
    void testGetLetters() {
        String russianLetters = AlphabetConfig.getLetters("russian");
        assertTrue(russianLetters.contains("а"));
        assertTrue(russianLetters.contains("я"));
    }

    @Test
    void testGetFullAlphabet() {
        String fullAlphabet = AlphabetConfig.getFullAlphabet("russian");
        assertTrue(fullAlphabet.contains("а")); // буквы
        assertTrue(fullAlphabet.contains("0")); // цифры
        assertTrue(fullAlphabet.contains(".")); // спецсимволы
    }
}
