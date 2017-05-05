package kze.backup.glacier.cmdargs;

public class EntryPointArgs {

    String inputDirectoryPath;
    String inputMonthsRange;
    String awsRegion;
    String awsGlacierVaultName;
    String awsAccessKeyId;
    String awsSecretAccessKey;
    String encryptionPassword;

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

    public String getEncryptionPassword() {
        return encryptionPassword;
    }

    @Override
    public String toString() {
        return "EntryPointArgs{" +
                "inputDirectoryPath='" + inputDirectoryPath + '\'' +
                ", inputMonthsRange='" + inputMonthsRange + '\'' +
                ", awsRegion='" + awsRegion + '\'' +
                ", awsGlacierVaultName='" + awsGlacierVaultName + '\'' +
                ", awsAccessKeyId='" + awsAccessKeyId + '\'' +
                ", awsSecretAccessKey='" + awsSecretAccessKey + '\'' +
                ", encryptionPassword='" + "**************" + '\'' +
                '}';
    }
}
