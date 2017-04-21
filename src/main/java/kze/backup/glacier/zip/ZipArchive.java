package kze.backup.glacier.zip;

import java.nio.file.Path;

public class ZipArchive {

    private Path inputPath;
    private Path zipPath;

    public ZipArchive(Path inputPath, Path zipPath) {
        this.inputPath = inputPath;
        this.zipPath = zipPath;
    }

    public Path getInputPath() {
        return inputPath;
    }

    public Path getZipPath() {
        return zipPath;
    }

    @Override
    public String toString() {
        return "ZipArchive{" +
                "inputPath=" + inputPath +
                ", zipPath=" + zipPath +
                '}';
    }
}
