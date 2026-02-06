package ru.javarush.berezhnoy.presentation.cli;

import picocli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Точка входа приложения.
 */
public class CaesarCliApp {
    private static final Logger logger = LogManager.getLogger(CaesarCliApp.class);

    public static void main(String[] args) {
        logger.info("Starting Caesar Cipher CLI");

        CaesarCli app = new CaesarCli();
        CommandLine cmd = new CommandLine(app);

        // Настройка вывода ошибок
        cmd.setExecutionExceptionHandler(new ExecutionExceptionHandler());

        // Запуск
        int exitCode = cmd.execute(args);

        logger.info("CLI exited with code: {}", exitCode);
        System.exit(exitCode);
    }

    /**
     * Обработчик исключений для красивого вывода ошибок.
     */
    static class ExecutionExceptionHandler implements CommandLine.IExecutionExceptionHandler {
        @Override
        public int handleExecutionException(Exception ex,
                                            CommandLine cmd,
                                            CommandLine.ParseResult parseResult) {
            // Уже обработанные ошибки (ParameterException) не логируем
            if (!(ex instanceof CommandLine.ParameterException)) {
                System.err.println("💥 Unexpected error: " + ex.getMessage());
                logger.error("Unexpected error", ex);
            }
            return cmd.getCommandSpec().exitCodeOnExecutionException();
        }
    }
}
