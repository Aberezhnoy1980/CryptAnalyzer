package ru.javarush.berezhnoy.presentation.cli.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import ru.javarush.berezhnoy.application.service.CipherServiceImpl;
import ru.javarush.berezhnoy.domain.exception.CaesarCipherException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Команда шифрования.
 */
@Command(
        name = "decrypt",
        description = "Decrypt a file using Caesar cipher"
)
public class DecryptCommand implements Callable<Integer> {
    private static final Logger logger = LogManager.getLogger(DecryptCommand.class);

    @Parameters(
            index = "0",
            paramLabel = "INPUT",
            description = "Input file to decrypt"
    )
    private Path inputFile;

    @Parameters(
            index = "1",
            paramLabel = "OUTPUT",
            description = "Output file for decrypted text"
    )
    private Path outputFile;

    @Option(
            names = {"-k", "--key"},
            required = true,
            description = "decryption key (shift value)",
            paramLabel = "KEY"
    )
    private int key;

    @Option(
            names = {"-v", "--verbose"},
            description = "Verbose output"
    )
    private boolean verbose;

    @Override
    public Integer call() throws Exception {
        try {
            validateFiles();

            logger.info("Starting decryption: {} -> {} (key: {})",
                    inputFile, outputFile, key);

            if (verbose) {
                System.out.printf("🔐 decrypting %s with key %d...%n",
                        inputFile.getFileName(), key);
            }

            CipherServiceImpl service = new CipherServiceImpl();
            service.decrypt(inputFile.toString(), outputFile.toString(), key);

            if (verbose) {
                System.out.println("✅ decryption completed successfully!");
                System.out.printf("   Input:  %s%n", inputFile.toAbsolutePath());
                System.out.printf("   Output: %s%n", outputFile.toAbsolutePath());
                System.out.printf("   Key:    %d%n", key);
            } else {
                System.out.println("decryption completed.");
            }

            return 0; // Успех

        } catch (CaesarCipherException | IllegalArgumentException e) {
            System.err.println("❌ Error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1; // Ошибка
        }
    }

    private void validateFiles() {
        if (!Files.exists(inputFile)) {
            throw new IllegalArgumentException("Input file does not exist: " + inputFile);
        }
        if (!Files.isReadable(inputFile)) {
            throw new IllegalArgumentException("Cannot read input file: " + inputFile);
        }
    }
}
