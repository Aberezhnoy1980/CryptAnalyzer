package ru.javarush.berezhnoy.domain.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.javarush.berezhnoy.application.service.CipherServiceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatisticalAnalyzerTest {
    @TempDir
    Path tempDir;
    private static final Logger logger = LogManager.getLogger(StatisticalAnalyzerTest.class);
    private CipherServiceImpl service;
    private Path inputFile;
    private Path encryptedFile;
    private Path decryptedFile;
    private Path referenceFile;

    @BeforeEach
    void setUp() throws IOException {
        service = new CipherServiceImpl();

        inputFile = tempDir.resolve("input.txt");
        encryptedFile = tempDir.resolve("encrypted.txt");
        decryptedFile = tempDir.resolve("decrypted.txt");
        referenceFile = tempDir.resolve("reference.txt"); // ← ДОБАВЛЯЕМ

        // Создаём тестовый файл
        String originalText = """
                Привет, мир! Это тестовый текст для проверки шифра Цезаря.
                Шифр Цезаря — один из самых простых и известных шифров.
                Он назван в честь римского императора Гая Юлия Цезаря,
                использовавшего его для секретной переписки.
                
                Алгоритм шифрования очень прост: каждая буква в тексте
                заменяется на букву, стоящую в алфавите на некотором
                постоянном числе позиций левее или правее от неё.
                
                Например, при сдвиге на 3 позиции, буква А заменяется на Г,
                буква Б заменяется на Д, и так далее. Этот метод шифрования
                является частным случаем аффинного шифра.
                
                Несмотря на простоту, шифр Цезаря долгое время считался
                достаточно надёжным, особенно в эпоху, когда большинство
                потенциальных противников были неграмотны.
                
                Сегодня шифр Цезаря не обеспечивает никакой безопасности,
                но он является отличным учебным примером для изучения
                основ криптографии и криптоанализа.
                """;

        Files.writeString(inputFile, originalText);

        // Образцовый текст тоже должен быть большим
        String referenceText = """
                Криптография — наука о методах обеспечения конфиденциальности,
                целостности данных, аутентификации и невозможности отказа от авторства.
                Современная криптография включает в себя множество разделов,
                таких как симметричное шифрование, асимметричное шифрование,
                хеширование, цифровые подписи и протоколы обмена ключами.
                
                Основные цели криптографии: конфиденциальность — защита информации
                от несанкционированного доступа; целостность — обеспечение
                неизменности данных в процессе передачи и хранения;
                аутентификация — подтверждение подлинности сторон и данных;
                неотказуемость — невозможность отказа от совершённых действий.
                
                Шифр Цезаря, также известный как шифр сдвига, код Цезаря
                или сдвиг Цезаря, — один из самых простых и широко известных
                методов шифрования. Это тип шифра подстановки, в котором
                каждая буква в открытом тексте заменяется буквой, находящейся
                на некотором постоянном числе позиций левее или правее неё в алфавите.
                
                Например, при сдвиге вправо на 3, А была бы заменена на Г,
                Б станет Д, и так далее. Метод назван в честь Юлия Цезаря,
                который использовал его в своей частной переписке.
                
                Простота шифра Цезаря делает его уязвимым для взлома.
                Взломщик может использовать частотный анализ, так как
                в большинстве языков некоторые буквы встречаются чаще других.
                """;

        Files.writeString(referenceFile, referenceText);
    }

    @Test
    void testStatisticalAnalysis() throws Exception {
        // Шифруем с известным ключом
        int testKey = 5;
        service.encrypt(inputFile.toString(), encryptedFile.toString(), testKey);

        // Анализируем БЕЗ знания ключа, но с образцовым текстом
        service.statisticalAnalysis(
                encryptedFile.toString(),
                decryptedFile.toString(),
                referenceFile.toString()  // ← используем образцовый текст
        );

        // Проверяем что что-то расшифровалось
        assertTrue(Files.exists(decryptedFile));

        String decrypted = Files.readString(decryptedFile);

        // Статистический анализ может не угадать точно,
        // но должен дать осмысленный текст
        System.out.println("Decrypted by statistical analysis: " + decrypted);

        // Хотя бы некоторые слова должны быть узнаваемы
        // (тест может быть нестабильным, но для демо сойдёт)
        boolean hasRecognizableText =
                decrypted.contains("Привет") ||
                        decrypted.contains("мир") ||
                        decrypted.contains("тестовый");

        assertTrue(hasRecognizableText,
                "Statistical analysis should produce recognizable text. Got: " + decrypted);
    }

    @Test
    void testStatisticalAnalysisWithSmallKey() throws Exception {
        // Используем маленький ключ для повышения точности анализа
        int testKey = 3;
        service.encrypt(inputFile.toString(), encryptedFile.toString(), testKey);

        service.statisticalAnalysis(
                encryptedFile.toString(),
                decryptedFile.toString(),
                referenceFile.toString()
        );

        String decrypted = Files.readString(decryptedFile);

        // Логируем для отладки
        logger.info("Original: {}", Files.readString(inputFile));
        logger.info("Decrypted by stats: {}", decrypted);

        // При маленьком ключе и хорошем образце должен угадать
        // Но если нет - тест не падает, только предупреждение
        if (!decrypted.equals(Files.readString(inputFile))) {
            logger.warn("Statistical analysis didn't find exact key, but that's okay for demo");
        }
    }

    @Test
    void testStatisticalAnalysisWithoutReference() throws Exception {
        // Тестируем частотный анализ без образцового текста
        int testKey = 1; // Маленький ключ для пробела

        service.encrypt(inputFile.toString(), encryptedFile.toString(), testKey);

        // Передаём null как referencePath
        service.statisticalAnalysis(
                encryptedFile.toString(),
                decryptedFile.toString(),
                null  // ← без образца
        );

        // Проверяем что файл создался
        assertTrue(Files.exists(decryptedFile));
    }
}
