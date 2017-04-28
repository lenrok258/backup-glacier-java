package kze.backup.glacier.encrypt;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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

/*
    AES uses 128-bit blocks
    IV (Init Vector) in CBC algorithm has to be of the size of single block
*/

// TODO: Switch to BouncyCastle's OpenSSLPBEParametersGenerator
public class OpenSslAes {

    private static final String SALTED_STR = "Salted__";
    private static final byte[] SALTED_MAGIC = SALTED_STR.getBytes(US_ASCII);

/*    public String encrypt(String password, String textToEncode) throws Exception {
        final byte[] inBytes = textToEncode.getBytes(UTF_8);
        byte[] outBytes = encrypt(password, inBytes, true);
        return Base64.getEncoder().encodeToString(outBytes);
    }*/

    public String decrypt(String password, String textToDecrypt) throws Exception {
        final byte[] inBytes = Base64.getDecoder().decode(textToDecrypt);
        byte[] openBytes = decrypt(password, inBytes);
        return new String(openBytes, UTF_8);
    }

    public String encrypt(String password, String textToEncode) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(textToEncode.getBytes(UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encrypt(password, inputStream, outputStream);
        byte[] outBytes = outputStream.toByteArray();
        return new String(Base64.getEncoder().encode(outBytes), UTF_8);
    }

/*    public String encrypt(String password, String textToEncode) throws Exception {
        final byte[] inBytes = textToEncode.getBytes(UTF_8);
        byte[] outBytes = encrypt(password, inBytes, true);
        return Base64.getEncoder().encodeToString(outBytes);
    }*/

    public void encrypt(String password, InputStream inputStream, OutputStream outputStream) throws Exception {
        final byte[] salt = (new SecureRandom()).generateSeed(8);
        final Cipher cipher = getCipher(password, salt, Cipher.ENCRYPT_MODE);

        // OpenSSL specific salt at the beginning of the file
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        bufferedOutputStream.write(array_concat(SALTED_MAGIC, salt));

        int chunkSize = cipher.getBlockSize() * 8 * 1024;
        byte[] input = new byte[chunkSize];
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        int bytesRead;
        while ((bytesRead = bufferedInputStream.read(input, 0, input.length)) != -1) {
            if (bytesRead < chunkSize) {
                bufferedOutputStream.write(cipher.doFinal(input));
            } else {
                bufferedOutputStream.write(cipher.update(input));
            }
        }
    }

/*    private byte[] encrypt(String password, byte[] bytesToEncode, boolean isFirstBlock) throws Exception {
        final byte[] salt = (new SecureRandom()).generateSeed(8);
        final Cipher cipher = getCipher(password, salt, Cipher.ENCRYPT_MODE);
        byte[] data = cipher.doFinal(bytesToEncode);
        if (isFirstBlock) {
            return array_concat(array_concat(SALTED_MAGIC, salt), data);
        } else {
            return data;
        }
    }*/

    private byte[] decrypt(String password, byte[] bytesToDecode) throws Exception {
        final byte[] shouldBeMagic = Arrays.copyOfRange(bytesToDecode, 0, SALTED_MAGIC.length);
        if (!Arrays.equals(shouldBeMagic, SALTED_MAGIC)) {
            throw new IllegalArgumentException("Initial bytes from input do not match OpenSSL SALTED_MAGIC salt value.");
        }
        final byte[] salt = Arrays.copyOfRange(bytesToDecode, SALTED_MAGIC.length, SALTED_MAGIC.length + 8);
        final Cipher cipher = getCipher(password, salt, Cipher.DECRYPT_MODE);
        return cipher.doFinal(bytesToDecode, 16, bytesToDecode.length - 16);
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

    public static void main(String[] args) throws Exception {
        OpenSslAes aes = new OpenSslAes();
        System.out.println(aes.encrypt("1234567890", "TEST"));
    }
}
