package ru.javarush.berezhnoy.infrastructure.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.javarush.berezhnoy.infrastructure.config.ApplicationConfig;
import ru.javarush.berezhnoy.infrastructure.config.SecurityConfig;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Высокопроизводительный процессор файлов с чанковой обработкой.
 * Использует Java NIO для эффективной работы с большими файлами.
 */
public class EfficientFileProcessor {
    private static final Logger logger = LogManager.getLogger(EfficientFileProcessor.class);

    // Константы для имён свойств
    private static final String PROP_BUFFER_SIZE_KB = "caesar.buffer.size.kb";
    private static final String PROP_FILE_ENCODING = "caesar.file.encoding";

    // Дефолтные значения
    private static final int DEFAULT_BUFFER_SIZE_KB = 32;
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final int MIN_BUFFER_SIZE_KB = 4;
    private static final int MAX_BUFFER_SIZE_KB = 1024;

    private final int bufferSize;
    private final Charset charset;

    /**
     * Создаёт процессор с настройками из конфигурации.
     */
    public EfficientFileProcessor() {
        this.bufferSize = getConfiguredBufferSize();
        this.charset = getConfiguredCharset();
        logger.debug("FileProcessor initialized: buffer={} bytes, charset={}",
                bufferSize, charset.name());
    }

    /**
     * Создаёт процессор с кастомными настройками.
     */
    public EfficientFileProcessor(int bufferSizeBytes, Charset charset) {
        validateBufferSize(bufferSizeBytes);
        validateCharset(charset);

        this.bufferSize = bufferSizeBytes;
        this.charset = charset;
        logger.debug("FileProcessor initialized with custom settings");
    }

    private int getConfiguredBufferSize() {
        try {
            // Читаем из конфига
            int sizeKB = ApplicationConfig.getIntProperty(
                    PROP_BUFFER_SIZE_KB,
                    DEFAULT_BUFFER_SIZE_KB
            );

            // Валидация
            if (sizeKB < MIN_BUFFER_SIZE_KB) {
                logger.warn("Buffer size too small ({}KB), using {}KB",
                        sizeKB, MIN_BUFFER_SIZE_KB);
                sizeKB = MIN_BUFFER_SIZE_KB;
            } else if (sizeKB > MAX_BUFFER_SIZE_KB) {
                logger.warn("Buffer size too large ({}KB), using {}KB",
                        sizeKB, MAX_BUFFER_SIZE_KB);
                sizeKB = MAX_BUFFER_SIZE_KB;
            }

            return sizeKB * 1024; // Конвертируем в байты

        } catch (Exception e) {
            logger.error("Failed to read buffer size from config, using default", e);
            return DEFAULT_BUFFER_SIZE_KB * 1024;
        }
    }

    private Charset getConfiguredCharset() {
        try {
            String encoding = ApplicationConfig.getProperty(
                    PROP_FILE_ENCODING,
                    DEFAULT_ENCODING
            );

            Charset charset = Charset.forName(encoding);
            logger.debug("Using charset: {}", charset.name());
            return charset;

        } catch (Exception e) {
            logger.error("Unsupported charset in config, using UTF-8", e);
            return StandardCharsets.UTF_8;
        }
    }

    private void validateBufferSize(int bufferSizeBytes) {
        if (bufferSizeBytes < MIN_BUFFER_SIZE_KB * 1024) {
            throw new IllegalArgumentException(
                    String.format("Buffer size too small: %d bytes (min: %d)",
                            bufferSizeBytes, MIN_BUFFER_SIZE_KB * 1024)
            );
        }
        if (bufferSizeBytes > MAX_BUFFER_SIZE_KB * 1024) {
            throw new IllegalArgumentException(
                    String.format("Buffer size too large: %d bytes (max: %d)",
                            bufferSizeBytes, MAX_BUFFER_SIZE_KB * 1024)
            );
        }
    }

    private void validateCharset(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("Charset cannot be null");
        }

        // Проверяем поддержку кодировки
        String[] supportedEncodings = {"UTF-8", "UTF-16", "Windows-1251", "ISO-8859-1"};
        boolean isSupported = false;
        for (String supported : supportedEncodings) {
            if (charset.name().equalsIgnoreCase(supported)) {
                isSupported = true;
                break;
            }
        }

        if (!isSupported) {
            logger.warn("Using unsupported charset: {}", charset.name());
        }
    }

    /**
     * Обрабатывает файл порциями (чанками) для экономии памяти.
     *
     * @param inputPath  путь к входному файлу
     * @param outputPath путь к выходному файлу
     * @param processor  процессор для обработки каждого символа
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public void processFileByChunks(String inputPath, String outputPath,
                                    CharProcessor processor) throws IOException {

        validatePaths(inputPath, outputPath);

        Path inPath = Paths.get(inputPath);
        Path outPath = Paths.get(outputPath);

        // Создаём родительские директории если нужно
        Files.createDirectories(outPath.getParent());

        try (FileChannel inputChannel = FileChannel.open(inPath, StandardOpenOption.READ);
             FileChannel outputChannel = FileChannel.open(outPath,
                     StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                     StandardOpenOption.TRUNCATE_EXISTING)) {

            CharsetDecoder decoder = charset.newDecoder();
            CharsetEncoder encoder = charset.newEncoder();
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bufferSize);
            CharBuffer charBuffer = CharBuffer.allocate(bufferSize / 2); // примерный размер

            long totalBytes = inputChannel.size();
            long processedBytes = 0;

            logger.info("Starting file processing: {} -> {} ({} bytes)",
                    inputPath, outputPath, totalBytes);

            while (inputChannel.read(byteBuffer) != -1) {
                byteBuffer.flip();

                // Декодируем байты в символы
                decoder.decode(byteBuffer, charBuffer, false);
                charBuffer.flip();

                // Обрабатываем символы
                processCharBuffer(charBuffer, processor);

                // Кодируем обратно в байты
                charBuffer.flip();
                ByteBuffer outputBuffer = encoder.encode(charBuffer);

                // Пишем в выходной файл
                while (outputBuffer.hasRemaining()) {
                    outputChannel.write(outputBuffer);
                }

                // Подготавливаем буферы для следующей порции
                byteBuffer.clear();
                charBuffer.clear();

                processedBytes += bufferSize;
                if (processedBytes % (10 * 1024 * 1024) == 0) { // Каждые 10MB логируем
                    logger.debug("Processed {} of {} bytes ({}%)",
                            processedBytes, totalBytes,
                            (processedBytes * 100 / totalBytes));
                }
            }

            logger.info("File processing completed: {} bytes processed", processedBytes);

        } catch (IOException e) {
            logger.error("Failed to process file {} -> {}", inputPath, outputPath, e);
            throw e;
        }
    }

    /**
     * Обрабатывает буфер символов.
     */
    private void processCharBuffer(CharBuffer buffer, CharProcessor processor) {
        while (buffer.hasRemaining()) {
            char original = buffer.get();
            char processed = processor.process(original);

            // Возвращаем обработанный символ на то же место
            buffer.position(buffer.position() - 1);
            buffer.put(processed);
        }
    }

    /**
     * Проверяет валидность путей файлов.
     */
    private void validatePaths(String inputPath, String outputPath) throws IOException {
        if (inputPath == null || outputPath == null) {
            throw new IllegalArgumentException("File paths cannot be null");
        }

        Path inPath = Paths.get(inputPath);
        if (!Files.exists(inPath)) {
            throw new IOException("Input file does not exist: " + inputPath);
        }

        if (!Files.isReadable(inPath)) {
            throw new IOException("Input file is not readable: " + inputPath);
        }

        // Защита от перезаписи системных файлов
        if (SecurityConfig.isPathProtected(outputPath) || SecurityConfig.isExtensionBlocked(outputPath)) {
            throw new SecurityException("Cannot write to system protected location: " + outputPath);
        }
    }

    /**
     * Интерфейс для обработки символов.
     */
    @FunctionalInterface
    public interface CharProcessor {
        char process(char input);
    }
}