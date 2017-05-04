package kze.backup.glacier.aws;

import static kze.backup.glacier.Logger.info;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    public GlacierArchiveInfoService(String vaultName) {
        this.vaultName = vaultName;
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
        info("Info file created [%s]", infoFilePath);
    }
}
