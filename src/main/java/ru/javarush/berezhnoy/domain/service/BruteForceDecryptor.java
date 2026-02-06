package ru.javarush.berezhnoy.domain.service;

import ru.javarush.berezhnoy.domain.model.CaesarCipher;
import ru.javarush.berezhnoy.domain.model.EnhancedAlphabet;
import ru.javarush.berezhnoy.domain.exception.CaesarCipherException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class BruteForceDecryptor {
    private static final Logger logger = LogManager.getLogger(BruteForceDecryptor.class);
    private static final Pattern WORD_PATTERN = Pattern.compile("[а-яА-ЯёЁ]{3,}");
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[А-ЯЁ].*?[.!?]");

    private final CaesarCipher cipher;

    public BruteForceDecryptor(CaesarCipher cipher) {
        this.cipher = cipher;
    }

    public int findKey(String encryptedText) {
        EnhancedAlphabet alphabet = cipher.getAlphabet();
        int alphabetSize = alphabet.getSize();
        int bestKey = 0;
        double bestScore = -1;

        logger.debug("Starting brute force attack on text of length {}",
                encryptedText.length());

        for (int key = 0; key < alphabetSize; key++) {
            String decrypted = cipher.decryptString(encryptedText, key);
            double score = evaluateDecryption(decrypted);

            if (score > bestScore) {
                bestScore = score;
                bestKey = key;
                logger.debug("New best key found: {} (score: {})", key, score);
            }
        }

        logger.info("Brute force completed. Best key: {} (score: {})", bestKey, bestScore);
        return bestKey;
    }

    private double evaluateDecryption(String text) {
        double score = 0;

        // 1. Количество "слов" (последовательностей букв)
        var wordMatcher = WORD_PATTERN.matcher(text);
        int wordCount = 0;
        while (wordMatcher.find()) wordCount++;
        score += wordCount * 0.5;

        // 2. Количество предложений
        var sentenceMatcher = SENTENCE_PATTERN.matcher(text);
        int sentenceCount = 0;
        while (sentenceMatcher.find()) sentenceCount++;
        score += sentenceCount * 2.0;

        // 3. Частота пробелов (должна быть разумной)
        long spaceCount = text.chars().filter(c -> c == ' ').count();
        double spaceRatio = (double) spaceCount / text.length();
        if (spaceRatio > 0.05 && spaceRatio < 0.25) {
            score += 10;
        }

        return score;
    }

    public void bruteForceFile(String inputPath, String outputPath)
            throws CaesarCipherException, IOException {
        String encryptedText = Files.readString(Path.of(inputPath));
        int key = findKey(encryptedText);

        logger.info("Decrypting with key: {}", key);
        cipher.decryptFile(inputPath, outputPath, key);
    }
}