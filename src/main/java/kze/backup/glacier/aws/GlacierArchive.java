package kze.backup.glacier.aws;

import com.amazonaws.services.glacier.transfer.UploadResult;

import kze.backup.glacier.encrypt.EncryptedArchive;

public class GlacierArchive {

    private EncryptedArchive encryptedArchive;
    private UploadResult uploadResult;

    public GlacierArchive(EncryptedArchive encryptedArchive, UploadResult uploadResult) {
        this.encryptedArchive = encryptedArchive;
        this.uploadResult = uploadResult;
    }

    public EncryptedArchive getEncryptedArchive() {
        return encryptedArchive;
    }

    public UploadResult getUploadResult() {
        return uploadResult;
    }

    @Override
    public String toString() {
        return "GlacierArchive{" +
                "encryptedArchive=" + encryptedArchive +
                ", uploadResult=" + uploadResult +
                '}';
    }
}
