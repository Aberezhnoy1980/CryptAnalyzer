package ru.javarush.berezhnoy.presentation.cli.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import ru.javarush.berezhnoy.application.service.CipherServiceImpl;
import ru.javarush.berezhnoy.domain.exception.CaesarCipherException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Encrypt command.
 */
@Command(
        name = "encrypt",
        description = "Encrypt a file using Caesar cipher"
)
public class EncryptCommand implements Callable<Integer> {
    private static final Logger logger = LogManager.getLogger(EncryptCommand.class);

    @Parameters(
            index = "0",
            paramLabel = "INPUT",
            description = "Input file to encrypt"
    )
    private Path inputFile;

    @Parameters(
            index = "1",
            paramLabel = "OUTPUT",
            description = "Output file for encrypted text"
    )
    private Path outputFile;

    @Option(
            names = {"-k", "--key"},
            required = true,
            description = "Encryption key (shift value)",
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

            logger.info("Starting encryption: {} -> {} (key: {})",
                    inputFile, outputFile, key);

            if (verbose) {
                System.out.printf("Encrypting %s with key %d...%n",
                        inputFile.getFileName(), key);
            }

            CipherServiceImpl service = new CipherServiceImpl();
            service.encrypt(inputFile.toString(), outputFile.toString(), key);

            if (verbose) {
                System.out.println("Encryption completed successfully.");
                System.out.printf("   Input:  %s%n", inputFile.toAbsolutePath());
                System.out.printf("   Output: %s%n", outputFile.toAbsolutePath());
                System.out.printf("   Key:    %d%n", key);
            } else {
                System.out.println("Encryption completed.");
            }

            return 0;

        } catch (CaesarCipherException | IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
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
