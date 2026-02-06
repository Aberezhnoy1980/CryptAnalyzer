package ru.javarush.berezhnoy.domain.model;

import ru.javarush.berezhnoy.domain.exception.InvalidKeyException;
import ru.javarush.berezhnoy.infrastructure.io.EfficientFileProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Caesar cipher implementation with file and string processing.
 */
public class CaesarCipher {
    private static final Logger logger = LogManager.getLogger(CaesarCipher.class);

    private final EnhancedAlphabet alphabet;
    private final EfficientFileProcessor fileProcessor;

    public CaesarCipher(EnhancedAlphabet alphabet) {
        this(alphabet, new EfficientFileProcessor());
    }

    public CaesarCipher(EnhancedAlphabet alphabet, EfficientFileProcessor fileProcessor) {
        if (alphabet == null) {
            throw new IllegalArgumentException("Alphabet cannot be null");
        }
        this.alphabet = alphabet;
        this.fileProcessor = fileProcessor != null ? fileProcessor : new EfficientFileProcessor();
        logger.debug("CaesarCipher initialized with alphabet size: {}", alphabet.getSize());
    }

    /**
     * Encrypts file from inputPath to outputPath using the given key.
     */
    public void encryptFile(String inputPath, String outputPath, int key) throws IOException, InvalidKeyException {
        validateKey(key);
        logger.info("Encrypting file {} -> {} with key {}", inputPath, outputPath, key);

        fileProcessor.processFileByChunks(inputPath, outputPath,
                inputChar -> encryptChar(inputChar, key));
    }

    /**
     * Decrypts file from inputPath to outputPath using the given key.
     */
    public void decryptFile(String inputPath, String outputPath, int key) throws IOException, InvalidKeyException {
        validateKey(key);
        logger.info("Decrypting file {} -> {} with key {}", inputPath, outputPath, key);

        fileProcessor.processFileByChunks(inputPath, outputPath,
                inputChar -> decryptChar(inputChar, key));
    }

    /**
     * Encrypts a single character.
     */
    public char encryptChar(char inputChar, int key) {
        int index = alphabet.indexOf(inputChar);
        if (index == -1) {
            return inputChar;
        }

        return alphabet.getSymbol(index + key);
    }

    /**
     * Decrypts a single character.
     */
    public char decryptChar(char inputChar, int key) {
        return encryptChar(inputChar, -key);
    }

    /**
     * Encrypts a string.
     */
    public String encryptString(String input, int key) throws InvalidKeyException {
        validateKey(key);
        if (input == null) return "";

        StringBuilder result = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            result.append(encryptChar(input.charAt(i), key));
        }

        return result.toString();
    }

    /**
     * Decrypts a string.
     */
    public String decryptString(String input, int key) throws InvalidKeyException {
        return encryptString(input, -key);
    }

    /**
     * Validates key; logs warnings for edge cases (0, Integer.MIN_VALUE).
     */
    public void validateKey(int key) throws InvalidKeyException {
        if (key == 0) {
            logger.warn("Key is 0 - no encryption/decryption will be performed");
        }
        if (key == Integer.MIN_VALUE) {
            logger.warn("Key is Integer.MIN_VALUE, may cause overflow in calculations");
        }
    }

    public EnhancedAlphabet getAlphabet() {
        return alphabet;
    }

    public int getAlphabetSize() {
        return alphabet.getSize();
    }
}