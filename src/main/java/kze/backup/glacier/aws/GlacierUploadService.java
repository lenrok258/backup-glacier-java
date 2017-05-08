package kze.backup.glacier.aws;

import static java.util.stream.Collectors.toList;
import static kze.backup.glacier.Logger.info;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.glacier.AmazonGlacier;
import com.amazonaws.services.glacier.AmazonGlacierClientBuilder;
import com.amazonaws.services.glacier.model.DataRetrievalPolicy;
import com.amazonaws.services.glacier.model.DataRetrievalRule;
import com.amazonaws.services.glacier.model.DeleteArchiveRequest;
import com.amazonaws.services.glacier.model.DeleteArchiveResult;
import com.amazonaws.services.glacier.model.DescribeJobRequest;
import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.DescribeVaultRequest;
import com.amazonaws.services.glacier.model.DescribeVaultResult;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;
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
    private final ArchiveTransferManager transferManager;
    private final String vaultName;
    private final GlacierArchiveInfoService archiveInfoService;

    public GlacierUploadService(String accessKey, String secretKey, String region, String vaultName) {
        this.vaultName = vaultName;
        this.glacier = buildGlacierClient(accessKey, secretKey, region);
        this.sqs = buildSqsClient(accessKey, secretKey, region);
        this.sns = buildSnsClient(accessKey, secretKey, region);
        this.transferManager = buildTransferManager();
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
            ProgressListener progressListener = new GlacierUploadProgressListener(encryptedArchive);
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

    /* **************************************************************
     * TEST AREA
     */

    private void printVault() {
        DescribeVaultRequest request = new DescribeVaultRequest("-", vaultName);
        DescribeVaultResult response = glacier.describeVault(request);
        System.out.println(response);
    }

    private void initJob() {
        JobParameters jobParameters = new JobParameters()
                .withArchiveId("vRcK-DwsZoymLL9JFhi4qz_5Veawmx-vsD8nPC2THcicCk_kdq4WdD5taaMz7QWCjEVQFjuoSD0YJq90CbDnyeF2NBcu3_0mm96asiMme6ZqGc9gjxVpF0HR6f0SKYj-Ub0b8Ov5Cg")
                .withType("archive-retrieval");
        InitiateJobRequest request = new InitiateJobRequest("-", vaultName, jobParameters);
        InitiateJobResult response = glacier.initiateJob(request);
        System.out.println(response);
    }

    private void checkJobStatus() {
        String jobId = "mO4ouEYoHB8O-U8jWFmT_znqgLCQWY4IqL7PBgEBrGEKYuo-E11hO1kZ9ZRlHP1JrdPsaTYdfmqvwADTGZgz0toleEmB";
        DescribeJobRequest request = new DescribeJobRequest("-", vaultName, jobId);
        DescribeJobResult response = glacier.describeJob(request);
        System.out.println(response);
    }

    public void listArchives() {
        InitiateJobRequest request = new InitiateJobRequest();
        request.setAccountId("-");
        request.setVaultName(vaultName);
        JobParameters jobParameters = new JobParameters();
        jobParameters.setType("inventory-retrieval");
        jobParameters.setFormat("JSON");
        request.setJobParameters(jobParameters);
        InitiateJobResult response = glacier.initiateJob(request);
        System.out.println(response);

        // JOBID: mO4ouEYoHB8O-U8jWFmT_znqgLCQWY4IqL7PBgEBrGEKYuo-E11hO1kZ9ZRlHP1JrdPsaTYdfmqvwADTGZgz0toleEmB
    }

    private void downloadArchive() {
        String jobId = "dz9p5wDGdwLSPjGGI8ZGiZzrBPb1_ZODBdzcoCfqnSwEauVIwWMzko_royA_nZjZ1r4-bnrrimaleo2wepn6mnN0r_XX";

        transferManager.downloadJobOutput(
                "-",
                vaultName,
                jobId,
                new File("/home/kornel/temp/test.zip_enc"));
    }

    public void getJobResult() throws IOException {
        String jobId = "mO4ouEYoHB8O-U8jWFmT_znqgLCQWY4IqL7PBgEBrGEKYuo-E11hO1kZ9ZRlHP1JrdPsaTYdfmqvwADTGZgz0toleEmB";
        GetJobOutputRequest request = new GetJobOutputRequest();
        request.setAccountId("-");
        request.setJobId(jobId);
        request.setVaultName(vaultName);
        GetJobOutputResult response = glacier.getJobOutput(request);
        System.out.println(response);

        InputStream body = response.getBody();
        String bodyString = IOUtils.toString(body, StandardCharsets.UTF_8.toString());
        System.out.println(bodyString);
    }

    private void deleteArchives() {
        Arrays.asList(
                "")
                .stream()
                .forEach(id -> {
                    DeleteArchiveRequest request = new DeleteArchiveRequest("-", vaultName, id);
                    DeleteArchiveResult response = glacier.deleteArchive(request);
                    System.out.println(response);
                    System.out.println("-----------------------------------------------------------------------------");
                });
    }

    public static void main(String[] args) throws IOException {
        GlacierUploadService service = new GlacierUploadService(
                "",
                "",
                "us-west-2",
                "test-vault");
//        service.printVault();
//        service.initJob();
//        service.checkJobStatus();
//        service.downloadArchive();
//        service.listArchives();
//        service.checkJobStatus();
//        service.getJobResult();
//        service.deleteArchives();

    }
}
