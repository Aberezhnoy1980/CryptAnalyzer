package ru.javarush.berezhnoy.presentation.cli.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import ru.javarush.berezhnoy.domain.exception.CaesarCipherException;
import ru.javarush.berezhnoy.domain.service.CipherService;
import ru.javarush.berezhnoy.presentation.cli.CaesarCli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Decrypt command (known key).
 */
@Command(
        name = "decrypt",
        description = "Decrypt a file using Caesar cipher"
)
public class DecryptCommand implements Callable<Integer> {
    private static final Logger logger = LogManager.getLogger(DecryptCommand.class);

    @ParentCommand
    private CaesarCli parent;

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
            description = "Decryption key (shift value)",
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
                System.out.printf("Decrypting %s with key %d...%n",
                        inputFile.getFileName(), key);
            }

            CipherService service = parent.getCipherService();
            service.decrypt(inputFile.toString(), outputFile.toString(), key);

            if (verbose) {
                System.out.println("Decryption completed successfully.");
                System.out.printf("   Input:  %s%n", inputFile.toAbsolutePath());
                System.out.printf("   Output: %s%n", outputFile.toAbsolutePath());
                System.out.printf("   Key:    %d%n", key);
            } else {
                System.out.println("Decryption completed.");
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
