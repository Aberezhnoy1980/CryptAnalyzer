package ru.javarush.berezhnoy.domain.service;

import ru.javarush.berezhnoy.domain.model.CaesarCipher;
import ru.javarush.berezhnoy.domain.model.EnhancedAlphabet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Statistical analyzer for Caesar cipher: frequency-based key recovery
 * with optional reference text comparison.
 */
public class StatisticalAnalyzer {
    private static final Logger logger = LogManager.getLogger(StatisticalAnalyzer.class);

    private static final int REFERENCE_SAMPLE_SIZE = 10_000;
    private static final char FALLBACK_REFERENCE_CHAR = 'о';

    private final CaesarCipher cipher;

    public StatisticalAnalyzer(CaesarCipher cipher) {
        this.cipher = cipher;
    }

    public int findKeyByFrequency(String encryptedText) {
        EnhancedAlphabet alphabet = cipher.getAlphabet();
        Map<Character, Integer> charCounts = new HashMap<>();
        int totalSymbols = 0;
        char mostFrequentChar = ' ';
        int maxCount = 0;

        for (char c : encryptedText.toCharArray()) {
            if (!alphabet.contains(c)) {
                continue;
            }
            totalSymbols++;
            int newCount = charCounts.getOrDefault(c, 0) + 1;
            charCounts.put(c, newCount);
            if (newCount > maxCount) {
                maxCount = newCount;
                mostFrequentChar = c;
            }
        }

        if (totalSymbols == 0) {
            logger.warn("No alphabet symbols found in text");
            return 0;
        }

        double percent = maxCount * 100.0 / totalSymbols;
        logger.debug("Most frequent char in encrypted text: '{}' ({} occurrences, {}%)",
                mostFrequentChar, maxCount, String.format("%.1f", percent));

        int encryptedIndex = alphabet.indexOf(mostFrequentChar);
        int spaceIndex = alphabet.indexOf(' ');
        if (spaceIndex == -1) {
            logger.warn("Space not found in alphabet, using '{}' as reference", FALLBACK_REFERENCE_CHAR);
            spaceIndex = alphabet.indexOf(FALLBACK_REFERENCE_CHAR);
        }

        if (encryptedIndex == -1 || spaceIndex == -1) {
            logger.warn("Cannot determine key, returning 0");
            return 0;
        }

        int key = encryptedIndex - spaceIndex;
        if (key < 0) {
            key += alphabet.getSize();
        }

        logger.info("Statistical analysis suggests key: {} (most frequent '{}' -> space)",
                key, mostFrequentChar);
        return key;
    }

    public int findKeyByReference(String encryptedText, String referenceText) {
        EnhancedAlphabet alphabet = cipher.getAlphabet();
        int alphabetSize = alphabet.getSize();

        String encryptedSample = encryptedText.length() > REFERENCE_SAMPLE_SIZE
                ? encryptedText.substring(0, REFERENCE_SAMPLE_SIZE)
                : encryptedText;
        String referenceSample = referenceText.length() > REFERENCE_SAMPLE_SIZE
                ? referenceText.substring(0, REFERENCE_SAMPLE_SIZE)
                : referenceText;

        Map<Character, Double> referenceFreq = calculateFrequencies(referenceSample);

        int bestKey = 0;
        double bestScore = Double.MAX_VALUE;

        for (int key = 0; key < alphabetSize; key++) {
            String decrypted = cipher.decryptString(encryptedSample, key);
            Map<Character, Double> decryptedFreq = calculateFrequencies(decrypted);
            double score = calculateDeviation(referenceFreq, decryptedFreq);

            if (score < bestScore) {
                bestScore = score;
                bestKey = key;
                logger.debug("Key {}: score {}", key, score);
            }
        }

        logger.info("Best key by reference: {} (score: {})", bestKey, bestScore);
        return bestKey;
    }

    private Map<Character, Double> calculateFrequencies(String text) {
        Map<Character, Integer> counts = new HashMap<>();
        EnhancedAlphabet alphabet = cipher.getAlphabet();
        int total = 0;

        for (char c : text.toCharArray()) {
            if (alphabet.contains(c)) {
                counts.put(c, counts.getOrDefault(c, 0) + 1);
                total++;
            }
        }

        Map<Character, Double> frequencies = new HashMap<>();
        if (total == 0) {
            return frequencies;
        }
        for (Map.Entry<Character, Integer> entry : counts.entrySet()) {
            frequencies.put(entry.getKey(), entry.getValue() / (double) total);
        }
        return frequencies;
    }

    private double calculateDeviation(Map<Character, Double> freq1, Map<Character, Double> freq2) {
        double deviation = 0;
        for (Character c : freq1.keySet()) {
            if (freq2.containsKey(c)) {
                double diff = freq1.get(c) - freq2.get(c);
                deviation += diff * diff;
            }
        }
        return deviation;
    }

    public void analyzeFile(String inputPath, String outputPath, String referencePath) throws IOException {
        String encryptedText = Files.readString(Path.of(inputPath));
        int key;

        if (referencePath != null && Files.exists(Path.of(referencePath))) {
            String referenceText = Files.readString(Path.of(referencePath));
            key = findKeyByReference(encryptedText, referenceText);
        } else {
            key = findKeyByFrequency(encryptedText);
        }

        logger.info("Decrypting with statistical key: {}", key);
        cipher.decryptFile(inputPath, outputPath, key);
    }
}
