package kze.backup.glacier.cmdargs;

import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.text.MessageFormat.format;
import static kze.backup.glacier.Logger.info;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;

import kze.backup.glacier.Logger;

public class CommandLineArgsService {

    private Path inputDirectoryPath;
    private String inputMonthsRange;
    private String awsRegion;
    private String awsGlacierVaultName;
    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private String encryptionPassword;

    public CommandLineArgsService(String[] cmdLineArgs) {
        parseCmdLineArgs(cmdLineArgs);
        askUserForSecrets();
        info(this);
    }

    public Path getInputDirectoryPath() {
        return inputDirectoryPath;
    }

    public String getInputMonthsRange() {
        return inputMonthsRange;
    }

    public String getAwsRegion() {
        return awsRegion;
    }

    public String getAwsGlacierVaultName() {
        return awsGlacierVaultName;
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public String getEncryptionPassword() {
        return encryptionPassword;
    }

    public String toString() {
        return String.format("Program parameters: [%s], [%s], [%s], [%s], [%s], [%s], [%s]",
                inputDirectoryPath,
                inputMonthsRange,
                awsRegion,
                awsGlacierVaultName,
                awsAccessKeyId,
                awsSecretAccessKey,
                encryptionPassword);
    }

    private void parseCmdLineArgs(String[] cmdLineArgs) {
        inputDirectoryPath = validateAndReturnInputDirectoryPath(cmdLineArgs[0]);
        inputMonthsRange = validateAndReturnInputMonthsRange(cmdLineArgs[1]);
        awsRegion = cmdLineArgs[2];
        awsGlacierVaultName = cmdLineArgs[3];
    }

    private Path validateAndReturnInputDirectoryPath(String inputDirectoryPath) {
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
        return path;
    }

    private String validateAndReturnInputMonthsRange(String inputMonthsRange) {
        if (isEmpty(inputMonthsRange)) {
            exitWithErrorMessage("Missing parameter 'months range'");
        }
        if (!inputMonthsRange.matches("^[01]?[0-9]{1}-[01]?[0-9]{1}$")) {
            exitWithErrorMessage("Incorrect parameter 'months range'");
        }
        return inputMonthsRange;
    }

    private void askUserForSecrets() {
        Console console = System.console();
        awsAccessKeyId = console.readLine("AWS access key id: ");
        awsSecretAccessKey = console.readLine("AWS secret access key: ");
        encryptionPassword = console.readLine("Encryption password: ");
    }

    private void exitWithErrorMessage(String errorMessage) {
        Logger.error(errorMessage);
        System.exit(-1);
    }

    public static void main(String[] args) {
        ParserProperties parserProperties = ParserProperties.defaults()
                .withUsageWidth(200)
                .withOptionValueDelimiter("=")
                .withShowDefaults(true);
        CommandLineArgs arguments = new CommandLineArgs();
        CmdLineParser parser = new CmdLineParser(arguments, parserProperties);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getLocalizedMessage());
            System.err.println();
            System.err.println("Options:");
            parser.printUsage(System.err);
        }
    }
}
