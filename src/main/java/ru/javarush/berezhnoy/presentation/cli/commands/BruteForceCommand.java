package ru.javarush.berezhnoy.presentation.cli.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import ru.javarush.berezhnoy.domain.service.CipherService;
import ru.javarush.berezhnoy.presentation.cli.CaesarCli;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Команда brute force атаки.
 */
@Command(
        name = "bruteforce",
        description = "Decrypt a file using brute force attack"
)
public class BruteForceCommand implements Callable<Integer> {
    @ParentCommand
    private CaesarCli parent;

    @Parameters(paramLabel = "INPUT", description = "Encrypted input file")
    private Path inputFile;

    @Parameters(paramLabel = "OUTPUT", description = "Output file for decrypted text")
    private Path outputFile;

    @Option(names = {"-v", "--verbose"}, description = "Verbose output")
    private boolean verbose;

    @Override
    public Integer call() throws Exception {
        try {
            System.out.println("Starting brute force attack...");

            CipherService service = parent.getCipherService();
            service.bruteForce(inputFile.toString(), outputFile.toString());

            System.out.println("Brute force completed.");
            System.out.printf("   Decrypted file: %s%n", outputFile.toAbsolutePath());

            return 0;
        } catch (Exception e) {
            System.err.println("Brute force failed: " + e.getMessage());
            return 1;
        }
    }
}
