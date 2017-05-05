package kze.backup.glacier;

import static kze.backup.glacier.Logger.error;
import static kze.backup.glacier.Logger.info;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;

import kze.backup.glacier.aws.GlacierUploadService;
import kze.backup.glacier.cmdargs.EntryPointArgs;
import kze.backup.glacier.cmdargs.EntryPointArgsService;
import kze.backup.glacier.encrypt.EncryptService;
import kze.backup.glacier.encrypt.EncryptedArchive;
import kze.backup.glacier.encrypt.VerifierService;
import kze.backup.glacier.zip.ZipArchive;
import kze.backup.glacier.zip.ZipService;

public class EntryPoint {

    public static void main(String[] cmdLineArgs) throws IOException {
        info("Started");

        // Arguments
        EntryPointArgs args = new EntryPointArgsService(cmdLineArgs).parse();
        info("Program args: [%s]", args);
        Path inputDir = Paths.get(args.getInputDirectoryPath());

        // Output path
        Path outputPath = prepareOutputDirectory(inputDir);

        // Compute paths to backup
        DirectoriesToBackup directoriesToBackup = new DirectoriesToBackup(inputDir, args.getInputMonthsRange());
        List<Path> pathsToBackup = directoriesToBackup.getPathsList();
        if (isEmpty(pathsToBackup)) {
            info("Nothing to backup. Exiting.");
            System.exit(0);
        }

        // Zip
        ZipService zipService = new ZipService(outputPath);
        List<ZipArchive> zipArchives = zipService.zipPaths(pathsToBackup);

        // Encrypt
        EncryptService encryptService = new EncryptService();
        List<EncryptedArchive> encArchives = encryptService.encZipArchives(
                args.getEncryptionPassword(), zipArchives);

        // Verify encrypted files
        VerifierService verifierService = new VerifierService();
        verifierService.verifyAll(args.getEncryptionPassword(), encArchives);

        // Upload to AWS Glacier
        GlacierUploadService glacierUploadService = new GlacierUploadService(args.getAwsAccessKeyId(),
                args.getAwsSecretAccessKey(),
                args.getAwsRegion(),
                args.getAwsGlacierVaultName());
        glacierUploadService.uploadAll(encArchives);

        // Clean up
        FileUtils.forceDelete(outputPath.toFile());
        info("Output directory [%s] deleted", outputPath);

        info("Finished");
    }

    private static Path prepareOutputDirectory(Path inputDirectoryPath) {
        Path output = Paths.get(inputDirectoryPath.toAbsolutePath().toString(), Config.DIR_NAME_OUTPUT);
        try {
            Files.createDirectory(output);
        } catch (FileAlreadyExistsException e) {
            info("Output directory [%s] already exists", output);
        } catch (IOException e) {
            error("Unable to create output directory [%s]", e, output);
            System.exit(-1);
        }
        info("Output path computed and created: %s", output.toAbsolutePath());
        return output;
    }

}
