package kze.backup.glacier;

import static java.text.MessageFormat.format;

import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EntryPointArgumentParser {

    private Path inputDirectoryPath;
    private String inputMonthsRange;
    private String awsRegion;
    private String awsGlacierVaultName;
    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private String encryptionPassword;

    public EntryPointArgumentParser(String[] cmdLineArgs) {
        parseCmdLineArgs(cmdLineArgs);
        // askUserForSecrets();
        Logger.info(this);
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
        inputMonthsRange = cmdLineArgs[1];
        awsRegion = cmdLineArgs[2];
        awsGlacierVaultName = cmdLineArgs[3];
    }

    private Path validateAndReturnInputDirectoryPath(String inputDirectoryPath) {
        if (inputDirectoryPath == null || inputDirectoryPath.isEmpty()) {
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
}