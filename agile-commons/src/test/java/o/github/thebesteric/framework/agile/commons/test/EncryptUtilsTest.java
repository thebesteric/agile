package o.github.thebesteric.framework.agile.commons.test;

import io.github.thebesteric.framework.agile.commons.util.EncryptUtils;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;

class EncryptUtilsTest {

    private static final byte[] FIXED_SALT = new byte[16];
    private static final byte[] AES_KEY = EncryptUtils.AES.deriveKey("strong-passphrase", FIXED_SALT, 256, 65536);

    // 验证 AES-GCM 加解密后明文一致且密文不同
    @Test
    void aesGcmShouldRoundTrip() {
        String plain = "hello-aes-gcm";
        String cipher = EncryptUtils.AES.encryptGCM(plain, AES_KEY);
        String decoded = EncryptUtils.AES.decryptGCM(cipher, AES_KEY);
        assertEquals(plain, decoded);
        assertNotEquals(plain, cipher); // ensure encryption actually changed the text
    }

    // 验证 AES-CBC 加解密后明文一致且密文不同
    @Test
    void aesCbcShouldRoundTrip() {
        String plain = "hello-aes-cbc";
        String cipher = EncryptUtils.AES.encryptCBC(plain, AES_KEY);
        String decoded = EncryptUtils.AES.decryptCBC(cipher, AES_KEY);
        assertEquals(plain, decoded);
        assertNotEquals(plain, cipher);
    }

    // 验证 BCrypt 哈希与校验逻辑正确
    @Test
    void bcryptShouldHashAndVerify() {
        String plain = "Pa$$w0rd!";
        String hashed = EncryptUtils.BCrypt.hash(plain);
        assertTrue(EncryptUtils.BCrypt.verify(plain, hashed));
        assertFalse(EncryptUtils.BCrypt.verify("wrong", hashed));
    }

    // 验证字符串/字节数组的 MD5 摘要与已知值一致
    @Test
    void md5HexStringAndBytesShouldMatchKnownValue() {
        String plain = "hello";
        String expected = "5d41402abc4b2a76b9719d911017c592"; // known MD5 of "hello"
        assertEquals(expected, EncryptUtils.MD5.hex(plain));
        assertEquals(expected, EncryptUtils.MD5.hex(plain.getBytes(StandardCharsets.UTF_8)));
    }

    // 验证文件/路径的 MD5 摘要与预期一致
    @Test
    void md5HexFileAndPathShouldMatchDigest() throws Exception {
        String content = "file-md5-content";
        Path temp = Files.createTempFile("encrypt-utils-md5", ".txt");
        Files.writeString(temp, content, StandardCharsets.UTF_8);

        String expected = toHex(MessageDigest.getInstance("MD5").digest(content.getBytes(StandardCharsets.UTF_8)));

        assertEquals(expected, EncryptUtils.MD5.hex(temp.toFile()));
        assertEquals(expected, EncryptUtils.MD5.hex(temp));

        Files.deleteIfExists(temp);
    }

    // 验证字符串的 SHA-256/512 摘要与标准实现一致
    @Test
    void shaHexStringShouldMatchKnownValue() throws Exception {
        String input = "hello-sha";
        String expected256 = toHex(MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8)));
        String expected512 = toHex(MessageDigest.getInstance("SHA-512").digest(input.getBytes(StandardCharsets.UTF_8)));

        assertEquals(expected256, EncryptUtils.SHA.hex256(input));
        assertEquals(expected512, EncryptUtils.SHA.hex512(input));
    }

    // 验证文件的 SHA-512 摘要与标准实现一致
    @Test
    void shaHexFileShouldMatchKnownValue() throws Exception {
        String content = "file-sha-content";
        Path temp = Files.createTempFile("encrypt-utils-sha", ".txt");
        Files.writeString(temp, content, StandardCharsets.UTF_8);

        String expected512 = toHex(MessageDigest.getInstance("SHA-512").digest(content.getBytes(StandardCharsets.UTF_8)));

        assertEquals(expected512, EncryptUtils.SHA.hex(temp.toFile(), true));
        Files.deleteIfExists(temp);
    }

    // 验证 HMAC-SHA256/512 结果与 JCE 标准实现一致
    @Test
    void hmacSha256And512ShouldMatchKnownValue() throws Exception {
        byte[] key = "secret-key".getBytes(StandardCharsets.UTF_8);
        String data = "hmac-payload";

        Mac hmac256 = Mac.getInstance("HmacSHA256");
        hmac256.init(new SecretKeySpec(key, "HmacSHA256"));
        String expected256 = toHex(hmac256.doFinal(data.getBytes(StandardCharsets.UTF_8)));

        Mac hmac512 = Mac.getInstance("HmacSHA512");
        hmac512.init(new SecretKeySpec(key, "HmacSHA512"));
        String expected512 = toHex(hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8)));

        assertEquals(expected256, EncryptUtils.HMAC.sha256Hex(key, data));
        assertEquals(expected512, EncryptUtils.HMAC.sha512Hex(key, data));
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
