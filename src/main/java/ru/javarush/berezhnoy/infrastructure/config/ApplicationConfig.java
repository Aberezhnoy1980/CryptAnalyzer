package ru.javarush.berezhnoy.infrastructure.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Centralized application configuration (properties).
 */
public class ApplicationConfig {
    private static final Logger logger = LogManager.getLogger(ApplicationConfig.class);
    private static final Properties properties = new Properties();

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        setDefaultProperties();
        try (InputStream input = ApplicationConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input != null) {
                properties.load(input);
                logger.info("Application configuration loaded from application.properties");
            }
        } catch (IOException e) {
            logger.warn("Could not load application.properties, using defaults", e);
        }
        properties.putAll(System.getProperties());
    }

    private static void setDefaultProperties() {
        properties.setProperty("caesar.alphabet.type", "russian");
        properties.setProperty("caesar.alphabet.russian.letters",
                "абвгдеёжзийклмнопрстуфхцчшщъыьэюя");
        properties.setProperty("caesar.alphabet.english.letters",
                "abcdefghijklmnopqrstuvwxyz");
        properties.setProperty("caesar.alphabet.digits", "0123456789");
        properties.setProperty("caesar.alphabet.special", " .,!?:;-()\"'«»—…");

        properties.setProperty("caesar.buffer.size.kb", "32");
        properties.setProperty("caesar.file.encoding", "UTF-8");

        properties.setProperty("caesar.security.protected.paths",
                "/etc/|/bin/|/sbin/|/windows/|/system32/|/program files/|C:\\Windows\\|C:\\Program Files\\");
        properties.setProperty("caesar.security.blocked.extensions",
                ".exe|.dll|.sys|.bat|.sh|.bash");

        properties.setProperty("caesar.logging.level", "INFO");
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getIntProperty(String key, int defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for property '{}', using default: {}",
                    key, defaultValue);
            return defaultValue;
        }
    }

    public static List<String> getListProperty(String key, String delimiter) {
        String value = properties.getProperty(key, "");
        if (value.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(delimiter));
    }

    static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    static void clearProperties() {
        properties.clear();
        setDefaultProperties();
    }
}
