package kze.backup.glacier;

import static kze.backup.glacier.Logger.info;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EntryPoint {

    public static final String FILENAME_AWS_ARCHIVE_INFO = "aws-archive-info.json";
    public static final String DIR_NAME_OUTPUT = "output";

    public static void main(String[] args) {

        info("Started");

        // Arguments
        EntryPointArgumentParser arguments = new EntryPointArgumentParser(args);

        // Output path
        Path outputPath = prepareOutputDirectory(arguments.getInputDirectoryPath());

        // Compute paths to backup
        List<Path> pathsToBackup = new DirectoriesToBackup(
                arguments.getInputDirectoryPath(),
                arguments.getInputMonthsRange(),
                FILENAME_AWS_ARCHIVE_INFO).getPathsList();
        if (isEmpty(pathsToBackup)) {
            info("Nothing to backup. Exiting.");
            System.exit(0);
        }

        // Zip

        // Encrypt

        // Verify encrypted files

        // Upload to AWS Glacier

        // Clean up

        info("Finished");

    }

    private static Path prepareOutputDirectory(Path inputDirectoryPath) {
        Path output = Paths.get(inputDirectoryPath.toAbsolutePath().toString(), DIR_NAME_OUTPUT);
        try {
            Files.createDirectory(output);
        } catch (FileAlreadyExistsException e) {
            info("Output directory [%s] already exists", output);
        } catch (IOException e) {
            Logger.error("Unable to create output directory [%s]", e, output);
            System.exit(-1);
        }
        info("Output path computed and created: %s", output.toAbsolutePath());
        return output;
    }

}
