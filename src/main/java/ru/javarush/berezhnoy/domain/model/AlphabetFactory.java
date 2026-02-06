package ru.javarush.berezhnoy.domain.model;

import ru.javarush.berezhnoy.infrastructure.config.AlphabetConfig;

/**
 * Factory for creating alphabets from config or custom strings.
 */
public class AlphabetFactory {

    public static EnhancedAlphabet createFromConfig() {
        String alphabetType = AlphabetConfig.getAlphabetType();
        return createAlphabet(alphabetType);
    }

    public static EnhancedAlphabet createAlphabet(String alphabetType) {
        String fullAlphabet = AlphabetConfig.getFullAlphabet(alphabetType);

        if (fullAlphabet.isEmpty()) {
            throw new IllegalArgumentException("No alphabet configured for type: " + alphabetType);
        }

        return new EnhancedAlphabet(fullAlphabet.toCharArray());
    }

    public static EnhancedAlphabet createCustomAlphabet(String symbols) {
        if (symbols == null || symbols.isEmpty()) {
            throw new IllegalArgumentException("Symbols cannot be null or empty");
        }
        return new EnhancedAlphabet(symbols.toCharArray());
    }

    public static EnhancedAlphabet createRussianAlphabet() {
        return createAlphabet("russian");
    }

    public static EnhancedAlphabet createEnglishAlphabet() {
        return createAlphabet("english");
    }
}
