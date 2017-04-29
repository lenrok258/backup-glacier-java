package kze.backup.glacier.zip;

import kze.backup.glacier.Logger;
import org.zeroturnaround.zip.ZipUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static kze.backup.glacier.Logger.info;

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
        info("Zip file created [%s], size [%s]", zipPath, getFileSizeMB(zipPath));
        return new ZipArchive(pathToZip, zipPath);
    }

    private Path computeOutputZipPath(Path pathToZip) {
        Path path = Paths.get(outputPath.toAbsolutePath().toString(),
                pathToZip.getFileName().toString() + FILENAME_POSTFIX_ZIP);
        info("Computed zip path [%s]", path);
        return path;
    }

    private String getFileSizeMB(Path zipPath) {
        try {
            long bytes = Files.size(zipPath);
            float mb = bytes / 1024 / 1024;
            return mb + "MB";
        } catch (IOException e) {
            Logger.error("Unable to read file's size [%s]", zipPath);
        }
        return "Unknown";
    }
}
