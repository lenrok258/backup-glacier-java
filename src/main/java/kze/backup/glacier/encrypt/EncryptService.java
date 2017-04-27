package kze.backup.glacier.encrypt;

import java.nio.file.Path;
import java.util.List;

import kze.backup.glacier.zip.ZipArchive;

public class EncryptService {

    private Path outputPath;

    public EncryptService(Path outputPath) {

        this.outputPath = outputPath;
    }

    public List<EncryptedArchive> encZipArchives(List<ZipArchive> zipArchives) {

        return null;
    }


}
