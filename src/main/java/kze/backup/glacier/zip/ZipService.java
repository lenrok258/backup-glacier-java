package kze.backup.glacier.zip;

import static java.util.stream.Collectors.toList;

import java.nio.file.Path;
import java.util.List;

public class ZipService {

    private static final String FILENAME_POSTFIX_ZIP = ".zip";

    private Path outputPath;

    public ZipService(Path outputPath) {
        this.outputPath = outputPath;
    }

    public List<ZipArchive> zipPaths(List<Path> pathsToBackup) {
        return pathsToBackup.stream()
                .map(this::createArchive)
                .collect(toList());
    }

    private ZipArchive createArchive(Path inputPath) {



        return new ZipArchive(inputPath, null);
    }
}
