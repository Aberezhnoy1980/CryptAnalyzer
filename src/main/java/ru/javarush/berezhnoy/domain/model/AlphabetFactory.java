package ru.javarush.berezhnoy.domain.model;

import ru.javarush.berezhnoy.domain.port.AlphabetProvider;

/**
 * Factory for creating alphabets (from a provider or custom symbols).
 */
public class AlphabetFactory {

    public static EnhancedAlphabet createFromConfig(AlphabetProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("AlphabetProvider cannot be null");
        }
        return provider.getDefaultAlphabet();
    }

    public static EnhancedAlphabet createAlphabet(AlphabetProvider provider, String alphabetType) {
        if (provider == null) {
            throw new IllegalArgumentException("AlphabetProvider cannot be null");
        }
        return provider.getAlphabet(alphabetType);
    }

    public static EnhancedAlphabet createCustomAlphabet(String symbols) {
        if (symbols == null || symbols.isEmpty()) {
            throw new IllegalArgumentException("Symbols cannot be null or empty");
        }
        return new EnhancedAlphabet(symbols.toCharArray());
    }
}
