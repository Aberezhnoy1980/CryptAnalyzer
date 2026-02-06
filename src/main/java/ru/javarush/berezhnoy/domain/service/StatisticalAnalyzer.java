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
 * Упрощённый статистический анализатор для шифра Цезаря.
 */
public class StatisticalAnalyzer {
    private static final Logger logger = LogManager.getLogger(StatisticalAnalyzer.class);

    // Ожидаемая частота символов в русском тексте (примерно)
    private static final Map<Character, Double> EXPECTED_FREQUENCIES = Map.of(
            ' ', 0.16,  // пробел
            'о', 0.09,
            'е', 0.08,
            'а', 0.08,
            'и', 0.07,
            'н', 0.06,
            'т', 0.06,
            'с', 0.05,
            'р', 0.05,
            'в', 0.04
    );

    private final CaesarCipher cipher;

    public StatisticalAnalyzer(CaesarCipher cipher) {
        this.cipher = cipher;
    }

    /**
     * Находит ключ методом частотного анализа.
     */
    public int findKeyByFrequency(String encryptedText) {
        EnhancedAlphabet alphabet = cipher.getAlphabet();

        // 1. Считаем частоты символов в зашифрованном тексте
        Map<Character, Integer> charCounts = new HashMap<>();
        int totalSymbols = 0;

        for (char c : encryptedText.toCharArray()) {
            if (alphabet.contains(c)) {
                charCounts.put(c, charCounts.getOrDefault(c, 0) + 1);
                totalSymbols++;
            }
        }

        if (totalSymbols == 0) {
            logger.warn("No alphabet symbols found in text");
            return 0;
        }

        // 2. Находим самый частый символ
        char mostFrequentChar = ' ';
        int maxCount = 0;

        for (Map.Entry<Character, Integer> entry : charCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequentChar = entry.getKey();
            }
        }

        logger.debug("Most frequent char in encrypted text: '{}' ({} occurrences, {:.1f}%)",
                mostFrequentChar, maxCount, (maxCount * 100.0 / totalSymbols));

        // 3. Предполагаем что самый частый символ - это пробел
        int encryptedIndex = alphabet.indexOf(mostFrequentChar);
        int spaceIndex = alphabet.indexOf(' ');

        if (spaceIndex == -1) {
            logger.warn("Space not found in alphabet, using 'о' as reference");
            spaceIndex = alphabet.indexOf('о');
        }

        if (encryptedIndex == -1 || spaceIndex == -1) {
            logger.warn("Cannot determine key, returning 0");
            return 0;
        }

        // 4. Вычисляем ключ
        int key = encryptedIndex - spaceIndex;
        if (key < 0) key += alphabet.getSize();

        logger.info("Statistical analysis suggests key: {} (most frequent '{}' -> space)",
                key, mostFrequentChar);

        return key;
    }

    /**
     * Находит ключ сравнивая с образцовым текстом.
     */
    public int findKeyByReference(String encryptedText, String referenceText) {
        EnhancedAlphabet alphabet = cipher.getAlphabet();
        int alphabetSize = alphabet.getSize();

        // 1. Считаем частоты в образцовом тексте
        Map<Character, Double> referenceFreq = calculateFrequencies(referenceText);

        // 2. Перебираем все возможные ключи
        int bestKey = 0;
        double bestScore = Double.MAX_VALUE;

        for (int key = 0; key < alphabetSize; key++) {
            // 3. Дешифруем с текущим ключом
            String decrypted = cipher.decryptString(encryptedText, key);

            // 4. Считаем частоты
            Map<Character, Double> decryptedFreq = calculateFrequencies(decrypted);

            // 5. Вычисляем отклонение
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
        for (Map.Entry<Character, Integer> entry : counts.entrySet()) {
            frequencies.put(entry.getKey(), entry.getValue() / (double) total);
        }

        return frequencies;
    }

    private double calculateDeviation(Map<Character, Double> freq1,
                                      Map<Character, Double> freq2) {
        double deviation = 0;

        // Для символов которые есть в обоих распределениях
        for (Character c : freq1.keySet()) {
            if (freq2.containsKey(c)) {
                double diff = freq1.get(c) - freq2.get(c);
                deviation += diff * diff; // Сумма квадратов отклонений
            }
        }

        return deviation;
    }

    public void analyzeFile(String inputPath, String outputPath,
                            String referencePath) throws IOException {
        String encryptedText = Files.readString(Path.of(inputPath));
        int key;

        if (referencePath != null && Files.exists(Path.of(referencePath))) {
            // Используем образцовый текст
            String referenceText = Files.readString(Path.of(referencePath));
            key = findKeyByReference(encryptedText, referenceText);
        } else {
            // Используем только частотный анализ
            key = findKeyByFrequency(encryptedText);
        }

        logger.info("Decrypting with statistical key: {}", key);
        cipher.decryptFile(inputPath, outputPath, key);
    }
}