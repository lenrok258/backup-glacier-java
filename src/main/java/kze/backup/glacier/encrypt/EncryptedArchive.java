package kze.backup.glacier.encrypt;

import kze.backup.glacier.zip.ZipArchive;

import java.nio.file.Path;

public class EncryptedArchive {

    private ZipArchive zipArchive;
    private Path encryptedArchivePath;

    public EncryptedArchive(ZipArchive zipArchive, Path encryptedArchivePath) {
        this.zipArchive = zipArchive;
        this.encryptedArchivePath = encryptedArchivePath;
    }

    public ZipArchive getZipArchive() {
        return zipArchive;
    }

    public Path getEncryptedArchivePath() {
        return encryptedArchivePath;
    }
}
