package kze.backup.glacier.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.glacier.AmazonGlacier;
import com.amazonaws.services.glacier.AmazonGlacierClientBuilder;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManagerBuilder;
import com.amazonaws.services.glacier.transfer.UploadResult;
import kze.backup.glacier.Logger;
import kze.backup.glacier.encrypt.EncryptedArchive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GlacierUploadService {

    private final AmazonGlacier glacier;
    private final String vaultName;
    private final GlacierArchiveInfoService glacierArchiveInfoService;

    public GlacierUploadService(String accessKey, String secretKey, String region,
                                String vaultName, String filenameAwsArchiveInfo) {
        this.vaultName = vaultName;
        this.glacier = buildGlacierClient(accessKey, secretKey, region);
        glacierArchiveInfoService = new GlacierArchiveInfoService(filenameAwsArchiveInfo);
    }

    public void uploadAll(List<EncryptedArchive> encryptedArchives) {
        encryptedArchives.stream()
                .forEach(this::upload);
    }

    private void upload(EncryptedArchive encryptedArchive) {
        Path pathToUpload = encryptedArchive.getPath();
        try {
            ArchiveTransferManager transferManager = buildTransferManager();
            ProgressListener progressListener = getProgressListener(encryptedArchive);
            Logger.info("About to upload [%s]", pathToUpload);
            UploadResult result = transferManager.upload(
                    "-",
                    vaultName,
                    encryptedArchive.computeDescription(),
                    pathToUpload.toFile(),
                    progressListener);
            String archiveId = result.getArchiveId();
            Logger.info("Archive id [%s] for file [%s]", archiveId, pathToUpload);
            // TODO: Save archiveId as JSON in source directory
        } catch (Exception e) {
            Logger.error("Uploading file [%s] failed", e, pathToUpload);
            System.exit(-1);
        }
    }

    private AmazonGlacier buildGlacierClient(String accessKey, String secretKey, String region) {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        return AmazonGlacierClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(region)
                .build();
    }

    private ArchiveTransferManager buildTransferManager() {
        return new ArchiveTransferManagerBuilder()
                .withGlacierClient(glacier)
                .build();
    }

    private ProgressListener getProgressListener(EncryptedArchive encryptedArchive) throws IOException {
        long sizeTotal = Files.size(encryptedArchive.getPath());
        return progressEvent -> {
            long bytesTransferred = progressEvent.getBytesTransferred();
            float percentage = ((float) bytesTransferred / (float) sizeTotal) * 100.0f;
            ProgressEventType eventType = progressEvent.getEventType();
            Logger.info("Glacier progress %s % [%s]", percentage, eventType);
        };
    }


}
