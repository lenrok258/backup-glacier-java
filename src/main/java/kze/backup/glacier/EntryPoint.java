package kze.backup.glacier;

import java.nio.file.Path;
import java.nio.file.Paths;

public class EntryPoint {

    public static void main(String[] args) {

        Logger.info("Started");

        EntryPointArgumentParser arguments = new EntryPointArgumentParser(args);
        Path outputPath = prepareOutputDirectory(arguments.getInputDirectoryPath());

        Logger.info("Finished");

    }

    private static Path prepareOutputDirectory(Path inputDirectoryPath) {
        Path output = Paths.get(inputDirectoryPath.toAbsolutePath().toString(), "output");
        Logger.info("Output path computed: %s", output.toAbsolutePath());
        return output;
    }
}
