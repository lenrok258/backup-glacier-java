package kze.backup.glacier.encrypt;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static kze.backup.glacier.Logger.info;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import kze.backup.glacier.Logger;

/*
    Notes:
    1. Key and IV generation is compliant with OpenSSL implementation
    2. AES uses 128-bit blocks
    3. IV (Init Vector) in CBC algorithm has to be of the size of single block
*/

// TODO: Switch to BouncyCastle's OpenSSLPBEParametersGenerator
public class OpenSslAes {

    private static final String SALTED_STR = "Salted__";
    private static final byte[] SALTED_MAGIC = SALTED_STR.getBytes(US_ASCII);

    public String encrypt(String password, String textToEncode) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(textToEncode.getBytes(UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encrypt(password, inputStream, outputStream);
        byte[] outBytes = outputStream.toByteArray();
        return new String(Base64.getEncoder().encode(outBytes), UTF_8);
    }

    public void encrypt(String password, InputStream inputStream, OutputStream outputStream) throws Exception {
        final byte[] salt = (new SecureRandom()).generateSeed(8);
        final Cipher cipher = getCipher(password, salt, Cipher.ENCRYPT_MODE);

        // OpenSSL specific salt at the beginning of the file
        outputStream.write(SALTED_MAGIC);
        outputStream.write(salt);

        processStream(inputStream, outputStream, cipher, "Encrypting");
    }

    public String decrypt(String password, String textToDecryptBase64) throws Exception {
        final byte[] inBytes = Base64.getDecoder().decode(textToDecryptBase64);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(inBytes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        decrypt(password, inputStream, outputStream);
        return new String(outputStream.toByteArray(), UTF_8);
    }

    public void decrypt(String password, InputStream inputStream, OutputStream outputStream) throws Exception {
        // Read magic string 'Salted__'
        byte[] magicSaltBytes = new byte[SALTED_MAGIC.length];
        inputStream.read(magicSaltBytes);
        if (!Arrays.equals(magicSaltBytes, SALTED_MAGIC)) {
            throw new IllegalArgumentException("Initial bytes from input do not match OpenSSL SALTED_MAGIC salt value.");
        }

        // Read salt value
        byte[] saltValue = new byte[8];
        inputStream.read(saltValue);

        // Decrypt
        final Cipher cipher = getCipher(password, saltValue, Cipher.DECRYPT_MODE);
        processStream(inputStream, outputStream, cipher, "Decrypting");
    }

    private byte[] generateKeyAnIV(byte[] passAndSalt) throws NoSuchAlgorithmException {
        byte[] hash = new byte[0];
        byte[] keyAndIv = new byte[0];
        for (int i = 0; i < 3 && keyAndIv.length < 48; i++) {
            final byte[] hashData = array_concat(hash, passAndSalt);
            final MessageDigest md = MessageDigest.getInstance("MD5");
            hash = md.digest(hashData);
            keyAndIv = array_concat(keyAndIv, hash);
        }
        return keyAndIv;
    }

    private Cipher getCipher(String password, byte[] salt, int cypherMode) throws Exception {
        final byte[] pass = password.getBytes(US_ASCII);
        final byte[] passAndSalt = array_concat(pass, salt);
        byte[] keyAndIv = generateKeyAnIV(passAndSalt);
        final byte[] keyValue = Arrays.copyOfRange(keyAndIv, 0, 32);
        final SecretKeySpec key = new SecretKeySpec(keyValue, "AES");
        final byte[] iv = Arrays.copyOfRange(keyAndIv, 32, 48);
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(cypherMode, key, new IvParameterSpec(iv));
        return cipher;
    }

    private byte[] array_concat(final byte[] a, final byte[] b) {
        final byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    private void processStream(InputStream inputStream,
                               OutputStream outputStream,
                               Cipher cipher,
                               String logMessage) throws Exception {
        int bytesTotal = inputStream.available();
        int bytesRead = 0;
        int bytesReadChunk;
        int chunkSize = cipher.getBlockSize() * 1024 * 1024; // 16B * 1028B * 1028B =~ 16MB
        info("Cipher chunk size %s Bytes", chunkSize);
        byte[] input = new byte[chunkSize];
        while ((bytesReadChunk = inputStream.read(input, 0, input.length)) != -1) {
            if (bytesReadChunk < chunkSize) {
                outputStream.write(cipher.doFinal(input, 0, bytesReadChunk));
            } else {
                outputStream.write(cipher.update(input, 0, bytesReadChunk));
            }
            bytesRead += bytesReadChunk;
            Logger.progress(logMessage, bytesRead, bytesTotal);
        }
        Logger.progressComplete();

        inputStream.close();
        outputStream.flush();
        outputStream.close();
    }

}
