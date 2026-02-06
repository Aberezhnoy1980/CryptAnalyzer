package ru.javarush.berezhnoy.infrastructure.config;

import ru.javarush.berezhnoy.domain.model.EnhancedAlphabet;
import ru.javarush.berezhnoy.domain.port.AlphabetProvider;

/**
 * AlphabetProvider implementation backed by AlphabetConfig.
 */
public class AlphabetConfigProvider implements AlphabetProvider {

    @Override
    public EnhancedAlphabet getDefaultAlphabet() {
        String type = AlphabetConfig.getAlphabetType();
        return getAlphabet(type);
    }

    @Override
    public EnhancedAlphabet getAlphabet(String type) {
        String fullAlphabet = AlphabetConfig.getFullAlphabet(type);
        if (fullAlphabet.isEmpty()) {
            throw new IllegalArgumentException("No alphabet configured for type: " + type);
        }
        return new EnhancedAlphabet(fullAlphabet.toCharArray());
    }
}
