package kze.backup.glacier.cmdargs;

import static java.text.MessageFormat.format;
import static kze.backup.glacier.Logger.error;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class EntryPointArgsService {

    private final String[] cmdLineArgs;

    public EntryPointArgsService(String[] cmdLineArgs) {
        this.cmdLineArgs = cmdLineArgs;
    }

    public EntryPointArgs parse() {
        EntryPointArgs args = new EntryPointArgs();

        parseCmdLineArgs(cmdLineArgs, args);
        askUserForSecrets(args);

        validateInputDirectoryPath(args.getInputDirectoryPath());
        validateInputMonthsRange(args.getInputMonthsRange());

        return args;
    }

    private void parseCmdLineArgs(String[] cmdLineArgs, EntryPointArgs args) {
        Options options = new Options();
        options.addRequiredOption("i", "input", true, "Input directory path");
        options.addRequiredOption("m", "months", true, "Months to backup (in format XX-XX)");
        options.addRequiredOption("r", "region", true, "AWS region");
        options.addRequiredOption("v", "vault", true, "AWS Glacier vault name");
        options.addRequiredOption("b", "info-backup-path", true, "AWS Glacier archive info files backup directory");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine values = parser.parse(options, cmdLineArgs);
            args.inputDirectoryPath = values.getOptionValue("i");
            args.inputMonthsRange = values.getOptionValue("m");
            args.awsRegion = values.getOptionValue("r");
            args.awsGlacierVaultName = values.getOptionValue("v");
            args.awsArchiveInfoFileBackupPath = values.getOptionValue("b");
        } catch (ParseException e) {
            error(e.getMessage());
            printHelp(options);
            System.exit(-1);
        }
    }

    private void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        System.out.println();
        helpFormatter.printHelp("run.sh", options);
        System.out.println();
    }

    private void askUserForSecrets(EntryPointArgs args) {
        Console console = System.console();
        args.awsAccessKeyId = console.readLine("AWS access key id: ");
        args.awsSecretAccessKey = console.readLine("AWS secret access key: ");
        args.encryptionPassword = console.readLine("Encryption password: ");
    }

    private void exitWithErrorMessage(String errorMessage) {
        error(errorMessage);
        System.exit(-1);
    }

    private void validateInputDirectoryPath(String inputDirectoryPath) {
        if (isEmpty(inputDirectoryPath)) {
            exitWithErrorMessage("Missing argument 'input directory'");
        }
        Path path = Paths.get(inputDirectoryPath);
        if (!Files.isDirectory(path)) {
            exitWithErrorMessage(format("Given input path [{0}] in not a directory", inputDirectoryPath));
        }
        if (Files.notExists(path)) {
            exitWithErrorMessage(format("Given input path [{0}] does not exist", inputDirectoryPath));
        }
    }

    private void validateInputMonthsRange(String inputMonthsRange) {
        if (isEmpty(inputMonthsRange)) {
            exitWithErrorMessage("Missing parameter 'months range'");
        }
        if (!inputMonthsRange.matches("^[01]?[0-9]{1}-[01]?[0-9]{1}$")) {
            exitWithErrorMessage("Incorrect parameter 'months range'");
        }
    }
}
