package kze.backup.glacier;

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

        Logger.info("Started");

        // Arguments
        EntryPointArgumentParser arguments = new EntryPointArgumentParser(args);

        // Output path
        Path outputPath = prepareOutputDirectory(arguments.getInputDirectoryPath());


        // Compute paths to backup
        List<Path> pathsToBackup = new DirectoriesToBackup(
                arguments.getInputDirectoryPath(),
                arguments.getInputMonthsRange(),
                FILENAME_AWS_ARCHIVE_INFO).getPathsList();
        if (pathsToBackup.size() == 0) {
            Logger.info("Nothing to backup. Exiting.");
            System.exit(0);
        }

        // Zip

        // Encrypt

        // Verify encrypted files

        // Upload to AWS Glacier

        // Clean up
        

        Logger.info("Finished");

    }

    private static Path prepareOutputDirectory(Path inputDirectoryPath) {
        Path output = Paths.get(inputDirectoryPath.toAbsolutePath().toString(), DIR_NAME_OUTPUT);
        try {
            Files.createDirectory(output);
        } catch (FileAlreadyExistsException e) {
            Logger.info("Output directory [%s] already exists", output);
        } catch (IOException e) {
            Logger.error("Unable to create output directory [%s]", e, output);
            System.exit(-1);
        }
        Logger.info("Output path computed and created: %s", output.toAbsolutePath());
        return output;
    }

}
