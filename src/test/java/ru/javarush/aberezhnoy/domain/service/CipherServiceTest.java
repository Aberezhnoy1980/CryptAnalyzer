package ru.javarush.aberezhnoy.domain.service;

import org.junit.jupiter.api.Test;
import ru.javarush.berezhnoy.domain.exception.CaesarCipherException;
import ru.javarush.berezhnoy.domain.service.CipherService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для интерфейса CipherService.
 * В основном проверяем контракт интерфейса.
 */
class CipherServiceTest {

    @Test
    void testCipherServiceContract() {
        // Тест на существование методов
        assertDoesNotThrow(() -> {
            CipherService service = new CipherService() {
                @Override
                public void encrypt(String inputPath, String outputPath, int key)
                        throws CaesarCipherException {}

                @Override
                public void decrypt(String inputPath, String outputPath, int key)
                        throws CaesarCipherException {}

                @Override
                public void bruteForce(String inputPath, String outputPath)
                        throws CaesarCipherException {}

                @Override
                public void statisticalAnalysis(String inputPath, String outputPath,
                                                String referenceTextPath)
                        throws CaesarCipherException {}
            };

            assertNotNull(service);
        });
    }
}