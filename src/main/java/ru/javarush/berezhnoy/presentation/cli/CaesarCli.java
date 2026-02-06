package ru.javarush.berezhnoy.presentation.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.javarush.berezhnoy.presentation.cli.commands.AnalyzeCommand;
import ru.javarush.berezhnoy.presentation.cli.commands.BruteForceCommand;
import ru.javarush.berezhnoy.presentation.cli.commands.DecryptCommand;
import ru.javarush.berezhnoy.presentation.cli.commands.EncryptCommand;

/**
 * Main CLI with subcommands (encrypt, decrypt, bruteforce, analyze).
 */
@Command(
        name = "caesar",
        mixinStandardHelpOptions = true,
        version = "Caesar Cipher 1.0",
        description = "Professional Caesar Cipher implementation",
        subcommands = {
                EncryptCommand.class,
                DecryptCommand.class,
                BruteForceCommand.class,
                AnalyzeCommand.class,
                CommandLine.HelpCommand.class
        }
)
public class CaesarCli implements Runnable {
    private static final Logger logger = LogManager.getLogger(CaesarCli.class);

    @Spec
    CommandSpec spec;

    @Override
    public void run() {
        throw new CommandLine.ParameterException(
                spec.commandLine(),
                "Please specify a command. Use 'caesar --help' for available commands."
        );
    }
}