package kze.backup.glacier.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import kze.backup.glacier.encrypt.EncryptedArchive;

public class GlacierArchiveInfoService {

    private final ObjectMapper objectMapper;
    private final String filenameAwsArchiveInfo;

    public GlacierArchiveInfoService(String filenameAwsArchiveInfo) {
        this.filenameAwsArchiveInfo = filenameAwsArchiveInfo;
        this.objectMapper = new ObjectMapper();
    }

    public void createInfoFile(EncryptedArchive encryptedArchive, String awsArchiveId) {

    }
}
