package ru.javarush.berezhnoy.domain.model;

import ru.javarush.berezhnoy.infrastructure.config.AlphabetConfig;

/**
 * Фабрика для создания алфавитов на основе конфигурации.
 */
public class AlphabetFactory {

    /**
     * Создаёт алфавит на основе конфигурации.
     */
    public static EnhancedAlphabet createFromConfig() {
        String alphabetType = AlphabetConfig.getAlphabetType();
        return createAlphabet(alphabetType);
    }

    /**
     * Создаёт алфавит указанного типа.
     */
    public static EnhancedAlphabet createAlphabet(String alphabetType) {
        String fullAlphabet = AlphabetConfig.getFullAlphabet(alphabetType);

        if (fullAlphabet.isEmpty()) {
            throw new IllegalArgumentException("No alphabet configured for type: " + alphabetType);
        }

        return new EnhancedAlphabet(fullAlphabet.toCharArray());
    }

    /**
     * Создаёт кастомный алфавит из строки символов.
     */
    public static EnhancedAlphabet createCustomAlphabet(String symbols) {
        if (symbols == null || symbols.isEmpty()) {
            throw new IllegalArgumentException("Symbols cannot be null or empty");
        }
        return new EnhancedAlphabet(symbols.toCharArray());
    }

    /**
     * Создаёт русский алфавит (удобный shortcut).
     */
    public static EnhancedAlphabet createRussianAlphabet() {
        return createAlphabet("russian");
    }

    /**
     * Создаёт английский алфавит (удобный shortcut).
     */
    public static EnhancedAlphabet createEnglishAlphabet() {
        return createAlphabet("english");
    }
}
