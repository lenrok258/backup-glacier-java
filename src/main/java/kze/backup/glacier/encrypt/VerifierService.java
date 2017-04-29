package kze.backup.glacier.encrypt;

import kze.backup.glacier.Logger;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
        Logger.info("About to verify file=[%s]", encryptedArchive.getEncryptedArchivePath());
        Path encPath = encryptedArchive.getEncryptedArchivePath();
        Path decPath = decrypt(password, encPath);
        boolean theSame = checkIfTheSame(encryptedArchive.getZipArchive().getZipPath(), decPath);
        if (!theSame) {
            Logger.error("Varying file failed. Decrypted file=[%s] is not the same as the one before encryption=[%s]", decPath, encPath);
            System.exit(-1);
        }
        Logger.info("File=[%s] verified", encryptedArchive.getEncryptedArchivePath());
    }

    private Path decrypt(String password, Path encPath) {
        Path decPath = Paths.get(encPath.toAbsolutePath() + DECRYPTED_FILE_POSTFIX);
        try {
            InputStream inputStream = Files.newInputStream(encPath);
            OutputStream outputStream = Files.newOutputStream(decPath);
            aes.decrypt(password, inputStream, outputStream);
        } catch (Exception e) {
            Logger.error("Unable to verify the file=[%s]", e, encPath);
        }
        return decPath;
    }

    private boolean checkIfTheSame(Path zipPath, Path decPath) {
        String zipMD5 = digestFileMD5(zipPath);
        Logger.info("MD5=[%s], File=[%s]", zipMD5, zipPath);
        String decMD5 = digestFileMD5(decPath);
        Logger.info("MD5=[%s], File=[%s]", decMD5, decPath);
        return zipMD5.equals(decMD5);
    }

    private String digestFileMD5(Path filePath) {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            return DigestUtils.md5Hex(inputStream);
        } catch (Exception e) {
            Logger.error("Unable to read file=[%s]", e, filePath);
            System.exit(-1);
        }
        return "";
    }

}
