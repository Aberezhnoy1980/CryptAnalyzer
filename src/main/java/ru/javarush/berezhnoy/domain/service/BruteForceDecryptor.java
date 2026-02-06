package ru.javarush.berezhnoy.domain.service;

import ru.javarush.berezhnoy.domain.model.CaesarCipher;
import ru.javarush.berezhnoy.domain.model.EnhancedAlphabet;
import ru.javarush.berezhnoy.domain.exception.CaesarCipherException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
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
        double bestScore = -Double.MAX_VALUE;

        // Берем только первые 1000 символов для скорости
        String textToAnalyze = encryptedText.length() > 1000
                ? encryptedText.substring(0, 1000)
                : encryptedText;

        logger.info("Brute force started. Testing {} keys.", alphabetSize);

        for (int key = 1; key < alphabetSize; key++) {
            String decrypted = cipher.decryptString(textToAnalyze, key);
            double score = evaluateDecryption(decrypted);

            // Логируем только хорошие варианты
            if (score > 200) {
                logger.info("Key {}: score {}:.1f - '{}'",
                        key, score,
                        decrypted.substring(0, Math.min(50, decrypted.length())));
            }

            if (score > bestScore) {
                bestScore = score;
                bestKey = key;
            }
        }

        logger.info("Brute force completed. Best key: {} (score: {}:.1f)",
                bestKey, bestScore);

        // Проверим результат
        String bestDecrypted = cipher.decryptString(textToAnalyze, bestKey);
        int commonWords = countCommonWords(bestDecrypted);
        logger.info("Common words found: {}", commonWords);

        if (bestScore < 250) {
            logger.warn("Low best score ({}:.1f). Result might be incorrect.", bestScore);
        }

        return bestKey;
    }

    private int countCommonWords(String text) {
        // Расширенный список частых русских слов
        Set<String> commonWords = Set.of(
                "и", "в", "не", "на", "я", "быть", "он", "с", "что", "а", "по", "это",
                "она", "этот", "но", "они", "мы", "как", "у", "к", "за",
                "вы", "так", "от", "все", "его", "вот", "о", "мне", "еще",
                "нет", "если", "меня", "только", "то", "когда", "уже", "для", "ты",
                "во", "со", "из", "или", "ли", "же", "бы", "до", "без", "над"
        );

        // Разбиваем текст на слова
        String[] words = text.toLowerCase()
                .replaceAll("[^а-яё\\s]", " ")  // Заменяем все не-буквы на пробелы
                .split("\\s+");

        int count = 0;
        for (String word : words) {
            if (!word.isEmpty() && commonWords.contains(word)) {
                count++;
            }
        }
        return count;
    }

    private double evaluateDecryption(String text) {
        double score = 0.0;

        // 1. Проверяем наличие русских букв
        long ruLetters = text.chars()
                .filter(c -> Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CYRILLIC)
                .count();
        double ruRatio = (double) ruLetters / text.length();

        if (text.length() == 0) return -1000;

        // Жестче: нужно хотя бы 60% русских букв
        if (ruRatio < 0.6) {
            return -1000;
        }
        score += ruRatio * 50;

        // 2. Ищем РЕАЛЬНЫЕ русские слова
        Pattern wordPattern = Pattern.compile("[а-яА-ЯёЁ]{3,}");
        Matcher wordMatcher = wordPattern.matcher(text);
        int validWordCount = 0;

        // Список самых частых русских слов
        Set<String> commonWords = Set.of(
                "и", "в", "не", "на", "я", "быть", "он", "с", "что", "а", "по", "это",
                "она", "этот", "но", "они", "мы", "как", "из", "у", "к", "до", "за",
                "вы", "так", "же", "от", "бы", "все", "его", "вот", "о", "мне", "еще",
                "нет", "если", "меня", "только", "то", "когда", "уже", "для", "ты"
        );

        while (wordMatcher.find()) {
            String word = wordMatcher.group().toLowerCase();

            // Слово должно иметь гласные
            if (!hasVowel(word)) {
                continue; // Пропускаем слова без гласных
            }

            validWordCount++;

            // Большой бонус за частые слова
            if (commonWords.contains(word)) {
                score += 100.0; // УВЕЛИЧИЛИ в 5 раз!
            }

            // Бонус за слова нормальной длины (3-10 букв)
            if (word.length() >= 3 && word.length() <= 10) {
                score += 10.0;
            }
        }

        score += validWordCount * 5.0;

        // 3. Частотный анализ букв
        String lowerText = text.toLowerCase();

        // Частые русские буквы должны встречаться часто
        int frequentMatches = 0;
        String frequentLetters = "оеаинтсрвлкмдпуяыьгзбчйхжшюцщэфъё";

        for (int i = 0; i < 10; i++) { // первые 10 самых частых букв
            char letter = frequentLetters.charAt(i);
            long count = lowerText.chars().filter(c -> c == letter).count();
            if (count > text.length() * 0.05) { // Должны быть >5%
                frequentMatches++;
            }
        }
        score += frequentMatches * 20.0;

        // 4. Соотношение гласных/согласных
        long vowels = lowerText.chars()
                .filter(c -> "аеёиоуыэюя".indexOf(c) >= 0)
                .count();
        long consonants = lowerText.chars()
                .filter(c -> "бвгджзйклмнпрстфхцчшщ".indexOf(c) >= 0)
                .count();

        if (consonants > 0) {
            double vowelConsonantRatio = (double) vowels / consonants;
            // В русском примерно 40% гласных, 60% согласных (0.4/0.6 ≈ 0.67)
            if (vowelConsonantRatio > 0.5 && vowelConsonantRatio < 0.8) {
                score += 100.0;
            } else {
                score -= 50.0; // Штраф за неправильное соотношение
            }
        }

        // 5. Штраф за нечитаемые последовательности
        Pattern consonantCluster = Pattern.compile("[бвгджзйклмнпрстфхцчшщ]{5,}",
                Pattern.CASE_INSENSITIVE);
        if (consonantCluster.matcher(text).find()) {
            score -= 200.0; // УВЕЛИЧИЛИ штраф!
        }

        // 6. Штраф за редкие буквы в начале слов
        Pattern startsWithRare = Pattern.compile("\\b[фщцэъё]\\w*", Pattern.CASE_INSENSITIVE);
        Matcher rareMatcher = startsWithRare.matcher(text);
        while (rareMatcher.find()) {
            score -= 30.0;
        }

        return score;
    }

    private boolean hasVowel(String word) {
        return word.matches(".*[аеёиоуыэюя].*");
    }

    public void bruteForceFile(String inputPath, String outputPath)
            throws CaesarCipherException, IOException {
        String encryptedText = Files.readString(Path.of(inputPath));
        int key = findKey(encryptedText);

        logger.info("Decrypting with key: {}", key);
        cipher.decryptFile(inputPath, outputPath, key);
    }
}