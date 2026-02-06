package ru.javarush.berezhnoy.domain.service;

import ru.javarush.berezhnoy.domain.model.CaesarCipher;
import ru.javarush.berezhnoy.domain.model.EnhancedAlphabet;
import ru.javarush.berezhnoy.domain.exception.CaesarCipherException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BruteForceDecryptor {
    private static final Logger logger = LogManager.getLogger(BruteForceDecryptor.class);

    private static final int SAMPLE_SIZE_CHARS = 1000;
    private static final double MIN_CYRILLIC_RATIO = 0.6;
    private static final double SCORE_EMPTY = -1000.0;
    private static final double BONUS_CYRILLIC_RATIO = 50.0;
    private static final double BONUS_COMMON_WORD = 100.0;
    private static final double BONUS_WORD_LENGTH = 10.0;
    private static final double BONUS_PER_VALID_WORD = 5.0;
    private static final double BONUS_FREQUENT_LETTER = 20.0;
    private static final double BONUS_VOWEL_CONSONANT = 100.0;
    private static final double PENALTY_VOWEL_CONSONANT = 50.0;
    private static final double PENALTY_CONSONANT_CLUSTER = 200.0;
    private static final double PENALTY_RARE_FIRST_LETTER = 30.0;
    private static final double THRESHOLD_LOG_GOOD_SCORE = 200.0;
    private static final double THRESHOLD_LOW_SCORE_WARN = 250.0;
    private static final double VOWEL_CONSONANT_RATIO_MIN = 0.5;
    private static final double VOWEL_CONSONANT_RATIO_MAX = 0.8;
    private static final double FREQUENT_LETTER_MIN_SHARE = 0.05;
    private static final int FREQUENT_LETTERS_COUNT = 10;
    private static final int MIN_WORD_LEN = 3;
    private static final int MAX_WORD_LEN = 10;
    private static final int CONSONANT_CLUSTER_MIN_LEN = 5;
    private static final int PREVIEW_LEN = 50;

    private static final Set<String> COMMON_WORDS = Set.of(
            "и", "в", "не", "на", "я", "быть", "он", "с", "что", "а", "по", "это",
            "она", "этот", "но", "они", "мы", "как", "у", "к", "за", "вы", "так",
            "от", "все", "его", "вот", "о", "мне", "еще", "нет", "если", "меня",
            "только", "то", "когда", "уже", "для", "ты", "во", "со", "из", "или",
            "ли", "же", "бы", "до", "без", "над"
    );

    private static final Pattern WORD_PATTERN = Pattern.compile("[а-яА-ЯёЁ]{3,}");
    private static final Pattern CONSONANT_CLUSTER = Pattern.compile(
            "[бвгджзйклмнпрстфхцчшщ]{" + CONSONANT_CLUSTER_MIN_LEN + ",}",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern STARTS_WITH_RARE = Pattern.compile("\\b[фщцэъё]\\w*", Pattern.CASE_INSENSITIVE);
    private static final String FREQUENT_LETTERS = "оеаинтсрвлкмдпуяыьгзбчйхжшюцщэфъё";
    private static final String VOWELS = "аеёиоуыэюя";
    private static final String CONSONANTS = "бвгджзйклмнпрстфхцчшщ";

    private final CaesarCipher cipher;

    public BruteForceDecryptor(CaesarCipher cipher) {
        this.cipher = cipher;
    }

    public int findKey(String encryptedText) {
        EnhancedAlphabet alphabet = cipher.getAlphabet();
        int alphabetSize = alphabet.getSize();
        int bestKey = 0;
        double bestScore = -Double.MAX_VALUE;

        String textToAnalyze = encryptedText.length() > SAMPLE_SIZE_CHARS
                ? encryptedText.substring(0, SAMPLE_SIZE_CHARS)
                : encryptedText;

        logger.info("Brute force started. Testing {} keys.", alphabetSize);

        for (int key = 1; key < alphabetSize; key++) {
            String decrypted = cipher.decryptString(textToAnalyze, key);
            double score = evaluateDecryption(decrypted);

            if (score > THRESHOLD_LOG_GOOD_SCORE) {
                String preview = decrypted.substring(0, Math.min(PREVIEW_LEN, decrypted.length()));
                logger.info("Key {}: score {} - '{}'", key, String.format("%.1f", score), preview);
            }

            if (score > bestScore) {
                bestScore = score;
                bestKey = key;
            }
        }

        logger.info("Brute force completed. Best key: {} (score: {})",
                bestKey, String.format("%.1f", bestScore));

        int commonWords = countCommonWords(cipher.decryptString(textToAnalyze, bestKey));
        logger.info("Common words found: {}", commonWords);

        if (bestScore < THRESHOLD_LOW_SCORE_WARN) {
            logger.warn("Low best score ({}). Result might be incorrect.",
                    String.format("%.1f", bestScore));
        }

        return bestKey;
    }

    private int countCommonWords(String text) {
        String[] words = text.toLowerCase()
                .replaceAll("[^а-яё\\s]", " ")
                .split("\\s+");
        int count = 0;
        for (String word : words) {
            if (!word.isEmpty() && COMMON_WORDS.contains(word)) {
                count++;
            }
        }
        return count;
    }

    private double evaluateDecryption(String text) {
        if (text == null || text.isEmpty()) {
            return SCORE_EMPTY;
        }

        int len = text.length();
        long ruLetters = text.chars()
                .filter(c -> Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CYRILLIC)
                .count();
        double ruRatio = (double) ruLetters / len;

        if (ruRatio < MIN_CYRILLIC_RATIO) {
            return SCORE_EMPTY;
        }

        double score = ruRatio * BONUS_CYRILLIC_RATIO;

        Matcher wordMatcher = WORD_PATTERN.matcher(text);
        int validWordCount = 0;

        while (wordMatcher.find()) {
            String word = wordMatcher.group().toLowerCase();
            if (!hasVowel(word)) {
                continue;
            }
            validWordCount++;
            if (COMMON_WORDS.contains(word)) {
                score += BONUS_COMMON_WORD;
            }
            if (word.length() >= MIN_WORD_LEN && word.length() <= MAX_WORD_LEN) {
                score += BONUS_WORD_LENGTH;
            }
        }

        score += validWordCount * BONUS_PER_VALID_WORD;

        String lowerText = text.toLowerCase();
        int frequentMatches = 0;
        for (int i = 0; i < Math.min(FREQUENT_LETTERS_COUNT, FREQUENT_LETTERS.length()); i++) {
            char letter = FREQUENT_LETTERS.charAt(i);
            long letterCount = lowerText.chars().filter(c -> c == letter).count();
            if (letterCount > len * FREQUENT_LETTER_MIN_SHARE) {
                frequentMatches++;
            }
        }
        score += frequentMatches * BONUS_FREQUENT_LETTER;

        long vowels = lowerText.chars().filter(c -> VOWELS.indexOf(c) >= 0).count();
        long consonants = lowerText.chars().filter(c -> CONSONANTS.indexOf(c) >= 0).count();
        if (consonants > 0) {
            double ratio = (double) vowels / consonants;
            if (ratio > VOWEL_CONSONANT_RATIO_MIN && ratio < VOWEL_CONSONANT_RATIO_MAX) {
                score += BONUS_VOWEL_CONSONANT;
            } else {
                score -= PENALTY_VOWEL_CONSONANT;
            }
        }

        if (CONSONANT_CLUSTER.matcher(text).find()) {
            score -= PENALTY_CONSONANT_CLUSTER;
        }

        Matcher rareMatcher = STARTS_WITH_RARE.matcher(text);
        while (rareMatcher.find()) {
            score -= PENALTY_RARE_FIRST_LETTER;
        }

        return score;
    }

    private boolean hasVowel(String word) {
        return word.matches(".*[" + VOWELS + "].*");
    }

    public void bruteForceFile(String inputPath, String outputPath) throws CaesarCipherException, IOException {
        Path path = Path.of(inputPath);
        String sample = readSample(path, SAMPLE_SIZE_CHARS);
        int key = findKey(sample);
        logger.info("Decrypting with key: {}", key);
        cipher.decryptFile(inputPath, outputPath, key);
    }

    private static String readSample(Path path, int maxChars) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            char[] buf = new char[Math.min(maxChars, 8192)];
            StringBuilder sb = new StringBuilder(maxChars);
            int total = 0;
            int n;
            while (total < maxChars && (n = reader.read(buf)) != -1) {
                int toAdd = Math.min(n, maxChars - total);
                sb.append(buf, 0, toAdd);
                total += toAdd;
            }
            return sb.toString();
        }
    }
}
