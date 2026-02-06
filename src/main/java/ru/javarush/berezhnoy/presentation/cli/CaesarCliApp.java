package ru.javarush.berezhnoy.presentation.cli;

import picocli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.javarush.berezhnoy.application.service.CipherServiceImpl;
import ru.javarush.berezhnoy.domain.service.CipherService;
import ru.javarush.berezhnoy.domain.port.AlphabetProvider;
import ru.javarush.berezhnoy.domain.port.FileProcessor;
import ru.javarush.berezhnoy.infrastructure.config.AlphabetConfigProvider;
import ru.javarush.berezhnoy.infrastructure.io.EfficientFileProcessor;

/**
 * Application entry point. Composes infrastructure and application layers.
 */
public class CaesarCliApp {
    private static final Logger logger = LogManager.getLogger(CaesarCliApp.class);

    public static void main(String[] args) {
        logger.info("Starting Caesar Cipher CLI");

        AlphabetProvider alphabetProvider = new AlphabetConfigProvider();
        FileProcessor fileProcessor = new EfficientFileProcessor();
        CipherService cipherService = new CipherServiceImpl(alphabetProvider, fileProcessor);

        CaesarCli app = new CaesarCli();
        app.setCipherService(cipherService);
        CommandLine cmd = new CommandLine(app);

        cmd.setExecutionExceptionHandler(new ExecutionExceptionHandler());
        int exitCode = cmd.execute(args);

        logger.info("CLI exited with code: {}", exitCode);
        System.exit(exitCode);
    }

    static class ExecutionExceptionHandler implements CommandLine.IExecutionExceptionHandler {
        @Override
        public int handleExecutionException(Exception ex,
                                            CommandLine cmd,
                                            CommandLine.ParseResult parseResult) {
            if (!(ex instanceof CommandLine.ParameterException)) {
                System.err.println("Unexpected error: " + ex.getMessage());
                logger.error("Unexpected error", ex);
            }
            return cmd.getCommandSpec().exitCodeOnExecutionException();
        }
    }
}
