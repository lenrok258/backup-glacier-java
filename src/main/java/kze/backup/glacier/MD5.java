package kze.backup.glacier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5 {

    public static String digest(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return DigestUtils.md5Hex(inputStream);
        } catch (Exception e) {
            Logger.error("Unable to read file=[%s]", e, path);
            System.exit(-1);
        }
        return "";
    }

}
