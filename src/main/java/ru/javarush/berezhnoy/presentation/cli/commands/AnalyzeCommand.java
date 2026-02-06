package ru.javarush.berezhnoy.presentation.cli.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import ru.javarush.berezhnoy.application.service.CipherServiceImpl;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Команда статистического анализа.
 */
@Command(
        name = "analyze",
        description = "Decrypt a file using statistical analysis"
)
public class AnalyzeCommand implements Callable<Integer> {
    @Parameters(paramLabel = "INPUT", description = "Encrypted input file")
    private Path inputFile;

    @Parameters(paramLabel = "OUTPUT", description = "Output file for decrypted text")
    private Path outputFile;

    @Option(
            names = {"-r", "--reference"},
            description = "Reference text file for analysis",
            paramLabel = "REFERENCE"
    )
    private Path referenceFile;

    @Option(names = {"-v", "--verbose"}, description = "Verbose output")
    private boolean verbose;

    @Override
    public Integer call() throws Exception {
        try {
            System.out.println("Starting statistical analysis...");

            CipherServiceImpl service = new CipherServiceImpl();
            String referencePath = referenceFile != null ? referenceFile.toString() : null;

            service.statisticalAnalysis(
                    inputFile.toString(),
                    outputFile.toString(),
                    referencePath
            );

            System.out.println("Statistical analysis completed.");
            System.out.printf("   Decrypted file: %s%n", outputFile.toAbsolutePath());
            if (referenceFile != null) {
                System.out.printf("   Used reference: %s%n", referenceFile.toAbsolutePath());
            }

            return 0;
        } catch (Exception e) {
            System.err.println("Statistical analysis failed: " + e.getMessage());
            return 1;
        }
    }
}
