package ru.javarush.berezhnoy.domain.port;

import java.io.IOException;

/**
 * Port for processing a file character-by-character (e.g. chunked read/transform/write).
 * Domain depends on this abstraction; infrastructure provides the implementation.
 */
public interface FileProcessor {

    void processFileByChunks(String inputPath, String outputPath, CharProcessor processor)
            throws IOException;

    @FunctionalInterface
    interface CharProcessor {
        char process(char input);
    }
}
