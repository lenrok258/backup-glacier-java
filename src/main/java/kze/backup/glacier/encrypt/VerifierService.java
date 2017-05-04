package kze.backup.glacier.encrypt;

import static kze.backup.glacier.Logger.error;
import static kze.backup.glacier.Logger.info;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import kze.backup.glacier.MD5;

public class VerifierService {

    private static final String DECRYPTED_FILE_POSTFIX = "_dec";

    private final OpenSslAes aes;

    public VerifierService() {
        aes = new OpenSslAes();
    }

    public void verifyAll(String password, List<EncryptedArchive> archiveList) {
        archiveList.stream()
                .forEach(archive -> verify(password, archive));
    }

    private void verify(String password, EncryptedArchive encryptedArchive) {
        info("About to verify file=[%s]", encryptedArchive.getPath());
        Path encPath = encryptedArchive.getPath();
        Path decPath = decrypt(password, encPath);
        boolean theSame = checkIfTheSame(encryptedArchive.getZipArchive().getZipPath(), decPath);
        if (!theSame) {
            error("Varying file failed. Decrypted file=[%s] is not the same as the one before encryption=[%s]", decPath, encPath);
            System.exit(-1);
        }
        info("File=[%s] verified", encryptedArchive.getPath());
    }

    private Path decrypt(String password, Path encPath) {
        Path decPath = Paths.get(encPath.toAbsolutePath() + DECRYPTED_FILE_POSTFIX);
        try {
            InputStream inputStream = Files.newInputStream(encPath);
            OutputStream outputStream = Files.newOutputStream(decPath);
            aes.decrypt(password, inputStream, outputStream);
        } catch (Exception e) {
            error("Unable to verify the file=[%s]", e, encPath);
        }
        return decPath;
    }

    private boolean checkIfTheSame(Path zipPath, Path decPath) {
        String zipMD5 = MD5.digest(zipPath);
        info("MD5=[%s], File=[%s]", zipMD5, zipPath);
        String decMD5 = MD5.digest(decPath);
        info("MD5=[%s], File=[%s]", decMD5, decPath);
        return zipMD5.equals(decMD5);
    }

}
