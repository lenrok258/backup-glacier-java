package kze.backup.glacier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.codec.digest.DigestUtils;

import static java.awt.SystemColor.info;
import static java.lang.System.exit;
import static kze.backup.glacier.Logger.error;
import static kze.backup.glacier.Logger.info;

public class MD5 {

    public static String digest(Path path) {
        info("Computing md5 for [%s]", path);
        try (InputStream inputStream = Files.newInputStream(path)) {
            return DigestUtils.md5Hex(inputStream);
        } catch (Exception e) {
            error("Unable to read file=[%s]", e, path);
            exit(-1);
        }
        return "";
    }

}
