package ru.javarush.berezhnoy.domain.model;

import ru.javarush.berezhnoy.domain.exception.InvalidKeyException;
import ru.javarush.berezhnoy.infrastructure.io.EfficientFileProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Реализация шифра Цезаря с поддержкой файловой обработки.
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
     * Шифрует файл.
     */
    public void encryptFile(String inputPath, String outputPath, int key) throws IOException, InvalidKeyException {
        validateKey(key);
        logger.info("Encrypting file {} -> {} with key {}", inputPath, outputPath, key);

        fileProcessor.processFileByChunks(inputPath, outputPath,
                inputChar -> encryptChar(inputChar, key));
    }

    /**
     * Дешифрует файл.
     */
    public void decryptFile(String inputPath, String outputPath, int key) throws IOException, InvalidKeyException {
        validateKey(key);
        logger.info("Decrypting file {} -> {} with key {}", inputPath, outputPath, key);

        fileProcessor.processFileByChunks(inputPath, outputPath,
                inputChar -> decryptChar(inputChar, key));
    }

    /**
     * Шифрует символ.
     */
    public char encryptChar(char inputChar, int key) {
        int index = alphabet.indexOf(inputChar);
        if (index == -1) {
            return inputChar;
        }

        return alphabet.getSymbol(index + key);
    }

    /**
     * Дешифрует символ.
     */
    public char decryptChar(char inputChar, int key) {
        return encryptChar(inputChar, -key);
    }

    /**
     * Шифрует строку.
     */
    public String encryptString(String input, int key) throws InvalidKeyException {
        validateKey(key);
        if (input == null) return "";

        StringBuilder result = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            result.append(encryptChar(input.charAt(i), key));
        }

        logger.debug("Encrypted string of length {} with key {}", input.length(), key);
        return result.toString();
    }

    /**
     * Дешифрует строку.
     */
    public String decryptString(String input, int key) throws InvalidKeyException {
        return encryptString(input, -key);
    }

    /**
     * Проверяет валидность ключа.
     */
    public void validateKey(int key) throws InvalidKeyException {
        // В шифре Цезаря любой int валиден
        // Но проверяем на крайние случаи
        if (key == 0) {
            logger.warn("Key is 0 - no encryption/decryption will be performed");
        }

        if (key == Integer.MIN_VALUE) {
            // Особый случай при инверсии
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