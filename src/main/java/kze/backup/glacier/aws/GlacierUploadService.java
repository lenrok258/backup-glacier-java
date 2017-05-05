package kze.backup.glacier.aws;

import static java.util.stream.Collectors.toList;
import static kze.backup.glacier.Logger.info;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.glacier.AmazonGlacier;
import com.amazonaws.services.glacier.AmazonGlacierClientBuilder;
import com.amazonaws.services.glacier.model.DataRetrievalPolicy;
import com.amazonaws.services.glacier.model.DataRetrievalRule;
import com.amazonaws.services.glacier.model.SetDataRetrievalPolicyRequest;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManagerBuilder;
import com.amazonaws.services.glacier.transfer.UploadResult;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import kze.backup.glacier.Logger;
import kze.backup.glacier.encrypt.EncryptedArchive;

public class GlacierUploadService {

    private final AmazonGlacier glacier;
    private final AmazonSQS sqs;
    private final AmazonSNS sns;
    private final String vaultName;
    private final GlacierArchiveInfoService archiveInfoService;

    public GlacierUploadService(String accessKey, String secretKey, String region, String vaultName) {
        this.vaultName = vaultName;
        this.glacier = buildGlacierClient(accessKey, secretKey, region);
        this.sqs = buildSqsClient(accessKey, secretKey, region);
        this.sns = buildSnsClient(accessKey, secretKey, region);
        this.archiveInfoService = new GlacierArchiveInfoService(vaultName);
    }

    public List<GlacierArchive> uploadAll(List<EncryptedArchive> encryptedArchives) {
        setDataRetrievalPolicyToFreeTierOnly();
        return encryptedArchives.stream()
                .map(this::upload)
                .map(this::createInfoFile)
                .collect(toList());
    }

    private GlacierArchive upload(EncryptedArchive encryptedArchive) {
        Path pathToUpload = encryptedArchive.getPath();
        try {
            ArchiveTransferManager transferManager = buildTransferManager();
            ProgressListener progressListener = getProgressListener(encryptedArchive);
            info("About to upload [%s]", pathToUpload);
            UploadResult result = transferManager.upload(
                    "-",
                    vaultName,
                    encryptedArchive.computeDescription(),
                    pathToUpload.toFile(),
                    progressListener);
            return new GlacierArchive(encryptedArchive, result);
        } catch (Exception e) {
            Logger.error("Uploading file [%s] failed", e, pathToUpload);
            System.exit(-1);
        }
        return null;
    }

    private GlacierArchive createInfoFile(GlacierArchive glacierArchive) {
        String archiveId = glacierArchive.getUploadResult().getArchiveId();
        Path uploadedPath = glacierArchive.getEncryptedArchive().getPath();
        info("Archive id [%s] for file [%s]", archiveId, uploadedPath);
        archiveInfoService.createInfoFile(glacierArchive);
        return glacierArchive;
    }

    private AmazonGlacier buildGlacierClient(String accessKey, String secretKey, String region) {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        return AmazonGlacierClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(region)
                .build();
    }

    private AmazonSQS buildSqsClient(String accessKey, String secretKey, String region) {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        return AmazonSQSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(region)
                .build();
    }

    private AmazonSNS buildSnsClient(String accessKey, String secretKey, String region) {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        return AmazonSNSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(region)
                .build();
    }

    private ArchiveTransferManager buildTransferManager() {
        return new ArchiveTransferManagerBuilder()
                .withGlacierClient(glacier)
                .withSqsClient(sqs)
                .withSnsClient(sns)
                .build();
    }

    private ProgressListener getProgressListener(EncryptedArchive encryptedArchive) throws IOException {
        long sizeTotal = Files.size(encryptedArchive.getPath());
        return progressEvent -> {
            long bytesTransferred = progressEvent.getBytesTransferred();
            float percentage = ((float) bytesTransferred / (float) sizeTotal) * 100.0f;
            ProgressEventType eventType = progressEvent.getEventType();
            info("Glacier progress %s % [%s]", percentage, eventType);
        };
    }

    private void setDataRetrievalPolicyToFreeTierOnly() {
        info("Setting up AWS Glacier retrieval data policy to free tier only");
        DataRetrievalRule rule = new DataRetrievalRule();
        rule.setStrategy("FreeTier");

        DataRetrievalPolicy policy = new DataRetrievalPolicy();
        policy.setRules(Arrays.asList(rule));

        SetDataRetrievalPolicyRequest request = new SetDataRetrievalPolicyRequest();
        request.setAccountId("-");
        request.setPolicy(policy);

        glacier.setDataRetrievalPolicy(request);
        info("Data policy set correctly");
    }
}
