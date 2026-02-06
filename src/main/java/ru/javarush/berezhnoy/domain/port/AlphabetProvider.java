package ru.javarush.berezhnoy.domain.port;

import ru.javarush.berezhnoy.domain.model.EnhancedAlphabet;

/**
 * Port for obtaining alphabets (e.g. from config).
 * Domain uses this abstraction; infrastructure provides the implementation.
 */
public interface AlphabetProvider {

    EnhancedAlphabet getDefaultAlphabet();

    EnhancedAlphabet getAlphabet(String type);
}
