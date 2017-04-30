package kze.backup.glacier.encrypt;

import kze.backup.glacier.zip.ZipArchive;

import java.nio.file.Path;

public class EncryptedArchive {

    private ZipArchive zipArchive;
    private Path path;

    public EncryptedArchive(ZipArchive zipArchive, Path path) {
        this.zipArchive = zipArchive;
        this.path = path;
    }

    public ZipArchive getZipArchive() {
        return zipArchive;
    }

    public Path getPath() {
        return path;
    }

    public String computeDescription() {
        return path.getFileName().toString();
    }

    @Override
    public String toString() {
        return "EncryptedArchive{" +
                "zipArchive=" + zipArchive +
                ", path=" + path +
                '}';
    }
}
