package ru.javarush.berezhnoy.application.service;

import ru.javarush.berezhnoy.domain.model.CaesarCipher;
import ru.javarush.berezhnoy.domain.model.EnhancedAlphabet;
import ru.javarush.berezhnoy.domain.port.AlphabetProvider;
import ru.javarush.berezhnoy.domain.port.FileProcessor;
import ru.javarush.berezhnoy.domain.exception.CaesarCipherException;
import ru.javarush.berezhnoy.domain.service.BruteForceDecryptor;
import ru.javarush.berezhnoy.domain.service.CipherService;
import ru.javarush.berezhnoy.domain.service.StatisticalAnalyzer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CipherServiceImpl implements CipherService {
    private static final Logger logger = LogManager.getLogger(CipherServiceImpl.class);
    private final CaesarCipher cipher;

    public CipherServiceImpl(AlphabetProvider alphabetProvider, FileProcessor fileProcessor) {
        if (alphabetProvider == null || fileProcessor == null) {
            throw new IllegalArgumentException("AlphabetProvider and FileProcessor cannot be null");
        }
        EnhancedAlphabet alphabet = alphabetProvider.getDefaultAlphabet();
        this.cipher = new CaesarCipher(alphabet, fileProcessor);
        logger.info("CipherService initialized with {} alphabet", cipher.getAlphabet().getSize());
    }

    @Override
    public void encrypt(String inputPath, String outputPath, int key) throws CaesarCipherException {
        try {
            cipher.encryptFile(inputPath, outputPath, key);
            logger.info("Encryption completed: {} -> {} (key: {})", inputPath, outputPath, key);
        } catch (Exception e) {
            throw new CaesarCipherException("Encryption failed", e);
        }
    }

    @Override
    public void decrypt(String inputPath, String outputPath, int key) throws CaesarCipherException {
        try {
            cipher.decryptFile(inputPath, outputPath, key);
            logger.info("Decryption completed: {} -> {} (key: {})", inputPath, outputPath, key);
        } catch (Exception e) {
            throw new CaesarCipherException("Decryption failed", e);
        }
    }

    @Override
    public void bruteForce(String inputPath, String outputPath) throws CaesarCipherException {
        try {
            BruteForceDecryptor decryptor = new BruteForceDecryptor(cipher);
            decryptor.bruteForceFile(inputPath, outputPath);
            logger.info("Brute force completed: {} -> {}", inputPath, outputPath);
        } catch (Exception e) {
            throw new CaesarCipherException("Brute force failed", e);
        }
    }

    @Override
    public void statisticalAnalysis(String inputPath, String outputPath, String referenceTextPath)
            throws CaesarCipherException {
        try {
            StatisticalAnalyzer analyzer = new StatisticalAnalyzer(cipher);
            analyzer.analyzeFile(inputPath, outputPath, referenceTextPath);
            logger.info("Statistical analysis completed: {} -> {} (reference: {})",
                    inputPath, outputPath, referenceTextPath);
        } catch (Exception e) {
            throw new CaesarCipherException("Statistical analysis failed", e);
        }
    }
}
