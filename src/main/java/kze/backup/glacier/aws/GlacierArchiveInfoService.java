package kze.backup.glacier.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kze.backup.glacier.Logger;

public class GlacierArchiveInfoService {

    private final ObjectMapper objectMapper;
    private final String vaultName;

    public GlacierArchiveInfoService(String vaultName) {
        this.vaultName = vaultName;
        this.objectMapper = new ObjectMapper();
    }

    public void createInfoFile(GlacierArchive glacierArchive) {
        GlacierArchiveInfo archiveInfo = map(glacierArchive);
        try {
            String json = objectMapper.writeValueAsString(archiveInfo);
            // TODO write json to filenameAwsArchiveInfo
        } catch (JsonProcessingException e) {
            Logger.error("Unable to write JSON file for [%s]", archiveInfo);
            System.exit(-1);
        }
    }

    private GlacierArchiveInfo map(GlacierArchive glacierArchive) {
        return null;
    }
}
