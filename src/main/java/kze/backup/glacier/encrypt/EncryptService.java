package kze.backup.glacier.encrypt;

import kze.backup.glacier.Logger;
import kze.backup.glacier.zip.ZipArchive;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class EncryptService {

    private static final String FILE_POSTFIX = "_enc";

    private OpenSslAes aes = new OpenSslAes();

    public List<EncryptedArchive> encZipArchives(List<ZipArchive> zipArchives, String password) {
        return zipArchives.stream()
                .map(zipArchive -> encZipArchive(zipArchive, password))
                .collect(toList());
    }

    private EncryptedArchive encZipArchive(ZipArchive zipArchive, String password) {
        Path zipPath = zipArchive.getZipPath();
        Path encArchivePath = Paths.get(zipArchive.getZipPath().toAbsolutePath() + FILE_POSTFIX);

        FileInputStream inputStream;
        FileOutputStream outputStream;
        try {
            inputStream = new FileInputStream(zipPath.toFile());
            outputStream = new FileOutputStream(encArchivePath.toFile());
            aes.encrypt(password, inputStream, outputStream);
        } catch (Exception e) {
            Logger.error("Unable to encrypt zip=[%s]", e, zipArchive);
            System.exit(-1);
        }

        return new EncryptedArchive(zipArchive, encArchivePath);
    }


}
