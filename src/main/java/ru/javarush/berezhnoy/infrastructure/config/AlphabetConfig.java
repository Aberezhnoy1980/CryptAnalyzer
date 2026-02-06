package ru.javarush.berezhnoy.infrastructure.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Конфигурация алфавитов для шифра Цезаря.
 * Использует централизованную конфигурацию из ApplicationConfig.
 */
public class AlphabetConfig {
    private static final Logger logger = LogManager.getLogger(AlphabetConfig.class);

    // Константы для имён свойств (не выносим в properties - это ключи)
    private static final String PROP_ALPHABET_TYPE = "caesar.alphabet.type";
    private static final String PROP_LETTERS_PREFIX = "caesar.alphabet.";
    private static final String PROP_LETTERS_SUFFIX = ".letters";
    private static final String PROP_DIGITS = "caesar.alphabet.digits";
    private static final String PROP_SPECIAL = "caesar.alphabet.special";

    // Дефолтные значения
    private static final String DEFAULT_ALPHABET_TYPE = "russian";
    private static final String DEFAULT_RUSSIAN_LETTERS = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    private static final String DEFAULT_ENGLISH_LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DEFAULT_DIGITS = "0123456789";
    private static final String DEFAULT_SPECIAL = " .,!?:;-()\"'«»—…";

    // Кэшированные значения для производительности
    private static volatile String cachedFullAlphabet;
    private static volatile String cachedAlphabetType;

    /**
     * Возвращает тип алфавита из конфигурации.
     */
    public static String getAlphabetType() {
        if (cachedAlphabetType == null) {
            synchronized (AlphabetConfig.class) {
                if (cachedAlphabetType == null) {
                    cachedAlphabetType = ApplicationConfig.getProperty(
                            PROP_ALPHABET_TYPE,
                            DEFAULT_ALPHABET_TYPE
                    ).toLowerCase();
                    logger.debug("Alphabet type loaded: {}", cachedAlphabetType);
                }
            }
        }
        return cachedAlphabetType;
    }

    /**
     * Возвращает буквы для указанного типа алфавита.
     */
    public static String getLetters(String alphabetType) {
        String propertyKey = PROP_LETTERS_PREFIX + alphabetType + PROP_LETTERS_SUFFIX;
        String letters = ApplicationConfig.getProperty(propertyKey, "");

        if (letters.isEmpty()) {
            // Возвращаем дефолтные значения для известных типов
            switch (alphabetType.toLowerCase()) {
                case "russian":
                    letters = DEFAULT_RUSSIAN_LETTERS;
                    break;
                case "english":
                    letters = DEFAULT_ENGLISH_LETTERS;
                    break;
                default:
                    logger.warn("No letters configured for alphabet type: {}", alphabetType);
            }
        }

        return letters;
    }

    /**
     * Возвращает цифры из конфигурации.
     */
    public static String getDigits() {
        return ApplicationConfig.getProperty(PROP_DIGITS, DEFAULT_DIGITS);
    }

    /**
     * Возвращает специальные символы из конфигурации.
     */
    public static String getSpecialSymbols() {
        return ApplicationConfig.getProperty(PROP_SPECIAL, DEFAULT_SPECIAL);
    }

    /**
     * Возвращает полный алфавит для указанного типа.
     * Алгоритм: буквы + цифры + специальные символы.
     */
    public static String getFullAlphabet(String alphabetType) {
        String letters = getLetters(alphabetType);
        String digits = getDigits();
        String special = getSpecialSymbols();

        if (letters.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot create alphabet: no letters configured for type: " + alphabetType
            );
        }

        String fullAlphabet = letters + digits + special;
        logger.debug("Created full alphabet for type '{}': {} symbols",
                alphabetType, fullAlphabet.length());

        return fullAlphabet;
    }

    /**
     * Возвращает полный алфавит для текущего типа из конфигурации.
     * Использует кэширование для производительности.
     */
    public static String getFullAlphabet() {
        if (cachedFullAlphabet == null) {
            synchronized (AlphabetConfig.class) {
                if (cachedFullAlphabet == null) {
                    String alphabetType = getAlphabetType();
                    cachedFullAlphabet = getFullAlphabet(alphabetType);
                    logger.info("Full alphabet cached: {} symbols for type '{}'",
                            cachedFullAlphabet.length(), alphabetType);
                }
            }
        }
        return cachedFullAlphabet;
    }

    /**
     * Возвращает список поддерживаемых типов алфавитов.
     */
    public static List<String> getSupportedAlphabetTypes() {
        return Arrays.asList("russian", "english", "custom");
    }

    /**
     * Проверяет, поддерживается ли указанный тип алфавита.
     */
    public static boolean isAlphabetTypeSupported(String alphabetType) {
        return getSupportedAlphabetTypes().contains(alphabetType.toLowerCase());
    }

    /**
     * Возвращает описание алфавита для логов/отладки.
     */
    public static String getAlphabetInfo() {
        String type = getAlphabetType();
        String fullAlphabet = getFullAlphabet();
        return String.format(
                "Alphabet type: '%s', Total symbols: %d (Letters: %d, Digits: %d, Special: %d)",
                type,
                fullAlphabet.length(),
                getLetters(type).length(),
                getDigits().length(),
                getSpecialSymbols().length()
        );
    }

    /**
     * Сбрасывает кэш конфигурации.
     * Полезно для тестирования или динамического обновления конфигурации.
     */
    public static void clearCache() {
        synchronized (AlphabetConfig.class) {
            cachedFullAlphabet = null;
            cachedAlphabetType = null;
            logger.debug("Alphabet configuration cache cleared");
        }
    }

    /**
     * Валидирует символы алфавита.
     * Проверяет на дубликаты и пустые символы.
     */
    public static void validateAlphabetSymbols(String symbols) {
        if (symbols == null || symbols.isEmpty()) {
            throw new IllegalArgumentException("Alphabet symbols cannot be null or empty");
        }

        // Проверяем на дубликаты
        for (int i = 0; i < symbols.length(); i++) {
            char c = symbols.charAt(i);
            if (symbols.indexOf(c, i + 1) != -1) {
                throw new IllegalArgumentException(
                        String.format("Duplicate symbol '%c' at positions %d and %d",
                                c, i, symbols.indexOf(c, i + 1))
                );
            }
        }

        logger.debug("Alphabet symbols validated: {} unique symbols", symbols.length());
    }

    /**
     * Для тестирования - устанавливает тип алфавита.
     */
    static void setAlphabetTypeForTest(String alphabetType) {
        synchronized (AlphabetConfig.class) {
            ApplicationConfig.setProperty(PROP_ALPHABET_TYPE, alphabetType);
            clearCache(); // Сбрасываем кэш
        }
    }
}