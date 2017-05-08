package kze.backup.glacier.aws;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import kze.backup.glacier.Logger;
import kze.backup.glacier.encrypt.EncryptedArchive;

import java.io.IOException;
import java.nio.file.Files;

import static com.amazonaws.event.ProgressEventType.REQUEST_BYTE_TRANSFER_EVENT;
import static com.amazonaws.event.ProgressEventType.TRANSFER_COMPLETED_EVENT;

public class GlacierUploadProgressListener implements ProgressListener {

    private long bytesTotal;
    private long bytesTransferred;

    public GlacierUploadProgressListener(EncryptedArchive encryptedArchive) throws IOException {
        bytesTotal = Files.size(encryptedArchive.getPath());
    }

    @Override
    public void progressChanged(ProgressEvent progressEvent) {
        bytesTransferred += progressEvent.getBytesTransferred();
        if (REQUEST_BYTE_TRANSFER_EVENT.equals(progressEvent.getEventType())) {
            Logger.progress("Uploading archive", bytesTransferred, bytesTotal);
        }
        if (TRANSFER_COMPLETED_EVENT.equals(progressEvent.getEventType())) {
            Logger.progressComplete();
        }
    }
}
