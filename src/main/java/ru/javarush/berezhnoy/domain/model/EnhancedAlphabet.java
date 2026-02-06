package ru.javarush.berezhnoy.domain.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Оптимизированный алфавит с гибридной стратегией поиска:
 * - O(1) для ASCII/BMP символов через массив
 * - O(1) для Unicode через HashMap
 */
public class EnhancedAlphabet {
    private static final int ASCII_LIMIT = 128; // Базовый ASCII
    private static final int BMP_LIMIT = 65536; // Basic Multilingual Plane

    private final char[] symbols;
    private final int[] charToIndex; // Для ASCII/BMP символов
    private final Map<Character, Integer> unicodeMap; // Для Unicode > BMP
    private final int size;

    public EnhancedAlphabet(char[] symbols) {
        if (symbols == null || symbols.length == 0) {
            throw new IllegalArgumentException("Alphabet symbols cannot be null or empty");
        }

        this.symbols = symbols.clone();
        this.size = symbols.length;

        // Определяем нужен ли нам HashMap для Unicode
        boolean needsUnicodeMap = false;
        for (char c : symbols) {
            if (c >= BMP_LIMIT) {
                needsUnicodeMap = true;
                break;
            }
        }

        if (needsUnicodeMap) {
            unicodeMap = new HashMap<>();
            charToIndex = new int[ASCII_LIMIT]; // Только для ASCII
        } else {
            unicodeMap = null;
            // Для BMP хватает массива
            int maxChar = 0;
            for (char c : symbols) {
                if (c > maxChar) maxChar = c;
            }
            charToIndex = new int[maxChar + 1];
        }

        Arrays.fill(charToIndex, -1);

        // Заполняем структуры данных
        for (int i = 0; i < size; i++) {
            char symbol = symbols[i];
            if (symbol < charToIndex.length) {
                charToIndex[symbol] = i;
            } else if (unicodeMap != null) {
                unicodeMap.put(symbol, i);
            }
        }
    }

    public int indexOf(char symbol) {
        if (symbol < charToIndex.length) {
            return charToIndex[symbol];
        } else if (unicodeMap != null) {
            return unicodeMap.getOrDefault(symbol, -1);
        }
        return -1;
    }

    // Остальные методы без изменений...
    public char getSymbol(int index) {
        int normalizedIndex = ((index % size) + size) % size;
        return symbols[normalizedIndex];
    }

    public int getSize() {
        return size;
    }

    public boolean contains(char symbol) {
        return indexOf(symbol) != -1;
    }

    public char[] getSymbols() {
        return symbols.clone();
    }
}
