package ru.javarush.berezhnoy.domain.service;

import ru.javarush.berezhnoy.domain.exception.CaesarCipherException;

/**
 * Сервис для операций шифрования и дешифрования.
 * Определяет контракт для всех алгоритмов шифрования в приложении.
 */
public interface CipherService {

    /**
     * Шифрует текст используя заданный ключ.
     *
     * @param inputPath  путь к файлу с исходным текстом
     * @param outputPath путь к файлу для записи зашифрованного текста
     * @param key        ключ шифрования (сдвиг)
     * @throws CaesarCipherException если произошла ошибка при шифровании
     */
    void encrypt(String inputPath, String outputPath, int key) throws CaesarCipherException;

    /**
     * Дешифрует текст используя заданный ключ.
     *
     * @param inputPath  путь к файлу с зашифрованным текстом
     * @param outputPath путь к файлу для записи расшифрованного текста
     * @param key        ключ шифрования (сдвиг)
     * @throws CaesarCipherException если произошла ошибка при дешифровании
     */
    void decrypt(String inputPath, String outputPath, int key) throws CaesarCipherException;

    /**
     * Дешифрует текст методом brute force (перебор всех ключей).
     *
     * @param inputPath  путь к файлу с зашифрованным текстом
     * @param outputPath путь к файлу для записи расшифрованного текста
     * @throws CaesarCipherException если произошла ошибка
     */
    void bruteForce(String inputPath, String outputPath) throws CaesarCipherException;

    /**
     * Дешифрует текст методом статистического анализа.
     *
     * @param inputPath         путь к файлу с зашифрованным текстом
     * @param outputPath        путь к файлу для записи расшифрованного текста
     * @param referenceTextPath путь к файлу с референсным текстом для анализа
     * @throws CaesarCipherException если произошла ошибка
     */
    void statisticalAnalysis(String inputPath, String outputPath, String referenceTextPath)
            throws CaesarCipherException;
}
