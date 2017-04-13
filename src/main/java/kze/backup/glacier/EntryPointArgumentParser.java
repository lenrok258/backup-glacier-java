package kze.backup.glacier;

import java.io.Console;

public class EntryPointArgumentParser {

    private String inputDirectoryPath;
    private String inputMonthsRange;
    private String awsRegion;
    private String awsGlacierVaultName;
    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private String encriptionPassword;

    public EntryPointArgumentParser(String[] cmdLineArgs) {
        // TODO: Validate count and print help id needed
        parseCmdLineArgs(cmdLineArgs);
        askUserForSecrets();
        printArguments();
    }

    private void parseCmdLineArgs(String[] cmdLineArgs) {
        inputDirectoryPath = cmdLineArgs[0];
        inputMonthsRange = cmdLineArgs[1];
        awsRegion = cmdLineArgs[2];
        awsGlacierVaultName = cmdLineArgs[3];
    }

    private void askUserForSecrets() {
        Console console = System.console();
        awsAccessKeyId = console.readLine("AWS access key id: ");
        awsSecretAccessKey = console.readLine("AWS secret access key: ");
        encriptionPassword = console.readLine("Encryption passpharse: ");
    }

    private void printArguments() {
        System.out.println("\n\n--------------------------------------\n");
        System.out.println("inputDirectoryPath: " + inputDirectoryPath);
        System.out.println("inputMonthsRange: " + inputMonthsRange);
        System.out.println("awsRegion: " + awsRegion);
        System.out.println("awsGlacierVaultName: " + awsGlacierVaultName);

        //TODO: Remove the ones below
        System.out.println("awsAccessKeyId: " + awsAccessKeyId);
        System.out.println("awsSecretAccessKey: " + awsSecretAccessKey);
        System.out.println("encriptionPassword: " + encriptionPassword);
        System.out.println("\n--------------------------------------\n\n");
    }

    public String getInputDirectoryPath() {
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

    public String getEncriptionPassword() {
        return encriptionPassword;
    }
}
