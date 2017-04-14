package kze.backup.glacier;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class EntryPoint {

    public static final String FILENAME_AWS_ARCHIVE_INFO = "aws-archive-info.json";

    public static void main(String[] args) {

        Logger.info("Started");

        EntryPointArgumentParser arguments = new EntryPointArgumentParser(args);

        Path outputPath = prepareOutputDirectory(arguments.getInputDirectoryPath());

        List<Path> pathsToBackup = computeDirectoriesToBackup(arguments.getInputDirectoryPath(), outputPath, FILENAME_AWS_ARCHIVE_INFO);

        Logger.info("Finished");

    }

    private static Path prepareOutputDirectory(Path inputDirectoryPath) {
        Path output = Paths.get(inputDirectoryPath.toAbsolutePath().toString(), "output");
        try {
            Files.createDirectory(output);
        } catch (FileAlreadyExistsException e) {
            Logger.info("Directory [%s] already exists", inputDirectoryPath);
        } catch (IOException e) {
            Logger.error("Unable to create output directory [%s]", e, inputDirectoryPath);
            System.exit(-1);
        }
        Logger.info("Output path computed and created: %s", output.toAbsolutePath());
        return output;
    }

    private static List<Path> computeDirectoriesToBackup(Path inputDirectoryPath, Path outputPath, String filenameAwsArchiveInfo) {
        return Collections.emptyList();
    }
}
