package ru.javarush.aberezhnoy.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.javarush.berezhnoy.domain.model.Alphabet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class AlphabetTest {
    private Alphabet alphabet;
    private char[] testSymbols;

    @BeforeEach
    void setUp() {
        testSymbols = new char[]{'а', 'б', 'в', 'г', 'д'};
        alphabet = new Alphabet(testSymbols);
    }

    @Test
    void testAlphabetCreation() {
        // Теперь alphabet уже инициализирован в setUp()
        assertEquals(5, alphabet.getSize());
        assertArrayEquals(testSymbols, alphabet.getSymbols());
    }

    @Test
    void testIndexOf() {
        assertEquals(0, alphabet.indexOf('а'));
        assertEquals(1, alphabet.indexOf('б'));
        assertEquals(2, alphabet.indexOf('в'));
        assertEquals(-1, alphabet.indexOf('я')); // не в алфавите
    }

    @Test
    void testGetSymbol() {
        assertEquals('а', alphabet.getSymbol(0));
        assertEquals('б', alphabet.getSymbol(1));
        assertEquals('а', alphabet.getSymbol(5));  // циклический сдвиг: 5 % 5 = 0
        assertEquals('д', alphabet.getSymbol(-1)); // циклический сдвиг: -1 → 4
    }

    @Test
    void testContains() {
        assertTrue(alphabet.contains('а'));
        assertTrue(alphabet.contains('д'));
        assertFalse(alphabet.contains('я'));
    }

    @Test
    void testEmptyAlphabetThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Alphabet(new char[]{});
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Alphabet(null);
        });
    }

    @ParameterizedTest
    @CsvSource({
            "0, а",
            "1, б",
            "6, б",  // 6 % 5 = 1
            "-1, д"  // -1 → 4
    })
    void testGetSymbolWithVariousInputs(int index, char expected) {
        assertEquals(expected, alphabet.getSymbol(index));
    }
}
