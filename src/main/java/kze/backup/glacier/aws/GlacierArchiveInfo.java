package kze.backup.glacier.aws;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GlacierArchiveInfo {

    @JsonProperty("aws_archive_id")
    public String awsArchiveId;

    @JsonProperty("aws_vault_name")
    public String awsVaultName;

    @JsonProperty("dir_name")
    public String dirName;

    @JsonProperty("dir_path")
    public String dirPath;

    @JsonProperty("zip_path")
    public String zipPath;

    @JsonProperty("enc_path")
    public String encPath;

    @JsonProperty("zip_hash")
    public String zipHash;

    @JsonProperty("enc_hash")
    public String encHash;

    @JsonProperty("zip_size")
    public String zipSize;

    @JsonProperty("enc_size")
    public String encSize;

}
