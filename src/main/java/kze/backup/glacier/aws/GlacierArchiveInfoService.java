package kze.backup.glacier.aws;

import static kze.backup.glacier.Logger.error;
import static kze.backup.glacier.Logger.info;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import kze.backup.glacier.Config;
import kze.backup.glacier.Logger;
import kze.backup.glacier.MD5;

public class GlacierArchiveInfoService {

    private final ObjectMapper objectMapper;
    private final String vaultName;
    private final String awsArchiveInfoFileBackupPath;

    public GlacierArchiveInfoService(String vaultName, String awsArchiveInfoFileBackupPath) {
        this.vaultName = vaultName;
        this.awsArchiveInfoFileBackupPath = awsArchiveInfoFileBackupPath;
        this.objectMapper = createObjectMapper();
    }

    public void createInfoFile(GlacierArchive glacierArchive) {
        try {
            GlacierArchiveInfo archiveInfo = map(glacierArchive);
            String json = objectMapper.writeValueAsString(archiveInfo);
            info(json);
            writeInfoFile(glacierArchive, json);
        } catch (Exception e) {
            Logger.error("Unable to write JSON file for archive [%s]", glacierArchive);
            System.exit(-1);
        }
    }

    private GlacierArchiveInfo map(GlacierArchive glacierArchive) throws IOException {
        GlacierArchiveInfo info = new GlacierArchiveInfo();
        info.awsArchiveId = glacierArchive.getUploadResult().getArchiveId();
        info.awsVaultName = this.vaultName;
        info.dirName = glacierArchive.getEncryptedArchive().getZipArchive().getInputPath().getFileName().toString();
        info.dirPath = glacierArchive.getEncryptedArchive().getZipArchive().getInputPath().toString();
        info.zipPath = glacierArchive.getEncryptedArchive().getZipArchive().getZipPath().toString();
        info.encPath = glacierArchive.getEncryptedArchive().getPath().toString();
        info.encSize = String.valueOf(Files.size(glacierArchive.getEncryptedArchive().getPath()));
        info.zipSize = String.valueOf(Files.size(glacierArchive.getEncryptedArchive().getZipArchive().getZipPath()));
        info.encHash = MD5.digest(glacierArchive.getEncryptedArchive().getPath());
        info.zipHash = MD5.digest(glacierArchive.getEncryptedArchive().getZipArchive().getZipPath());
        return info;
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }

    private void writeInfoFile(GlacierArchive glacierArchive, String json) throws IOException {
        Path inputPath = glacierArchive.getEncryptedArchive().getZipArchive().getInputPath();
        Path infoFilePath = Paths.get(inputPath.toString(), Config.FILENAME_AWS_ARCHIVE_INFO);
        Files.write(infoFilePath, json.getBytes(StandardCharsets.UTF_8));
        writeInfoBackupFile(glacierArchive, awsArchiveInfoFileBackupPath, json);
        info("Info file created [%s]", infoFilePath);
    }

    private void writeInfoBackupFile(GlacierArchive glacierArchive, String backupDirectory, String json) throws IOException {
        String zipArchiveName = glacierArchive.getEncryptedArchive().getZipArchive().getZipPath().getFileName().toString();
        Path backupDirPath = Paths.get(backupDirectory);
        if (!backupDirPath.toFile().exists()) {
            try {
                Files.createDirectory(backupDirPath);
            } catch (FileAlreadyExistsException e) {
                info("Output directory [%s] already exists", backupDirPath);
            } catch (IOException e) {
                error("Unable to create output directory [%s]. Info files will not be backed-up", e, backupDirPath);
            }
        }
        Path infoFileBackupPath = Paths.get(backupDirPath.toAbsolutePath().toString(), zipArchiveName + ".json");
        Files.write(infoFileBackupPath, json.getBytes(StandardCharsets.UTF_8));
        info("Info file *backup* created [%s]", infoFileBackupPath);
    }
}

