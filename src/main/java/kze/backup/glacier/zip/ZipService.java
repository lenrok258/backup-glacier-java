package kze.backup.glacier.zip;

import static java.util.stream.Collectors.toList;
import static kze.backup.glacier.Logger.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.zeroturnaround.zip.ZipUtil;

import kze.backup.glacier.Logger;

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

    private ZipArchive createArchive(Path pathToZip) {
        Path zipPath = computeOutputZipPath(pathToZip);
        ZipUtil.pack(pathToZip.toFile(), zipPath.toFile());
        info("Zip file created [%s]", zipPath);
        return new ZipArchive(pathToZip, zipPath);
    }

    private Path computeOutputZipPath(Path pathToZip) {
        Path path = Paths.get(outputPath.toAbsolutePath().toString(),
                pathToZip.getFileName().toString() + FILENAME_POSTFIX_ZIP);
        info("Computed zip path [%s]", path);
        return path;
    }
}
