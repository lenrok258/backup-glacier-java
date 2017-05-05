package kze.backup.glacier.cmdargs;

import org.kohsuke.args4j.Option;

public class CommandLineArgs {

    @Option(name = "-input",
            aliases = {"-i"},
            usage = "Input directory path",
            required = true)
    private String inputDirectoryPath;

    @Option(name = "-months",
            aliases = {"-m"},
            usage = "Months to backup (in format XX-XX)",
            required = true)
    private String inputMonthsRange;

    @Option(name = "-region",
            aliases = {"-r"},
            usage = "AWS region",
            required = true)
    private String awsRegion;

    @Option(name = "-vault",
            aliases = {"-v"},
            usage = "AWS region",
            required = true)
    private String awsGlacierVaultName;

    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private String encryptionPassword;
}
