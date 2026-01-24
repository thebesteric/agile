package io.github.thebesteric.framework.agile.commons.util;

import io.github.thebesteric.framework.agile.commons.exception.DecryptException;
import io.github.thebesteric.framework.agile.commons.exception.EncryptException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * 加解密工具类
 * 使用建议：
 * - MD5：仅适合非安全场景的校验/去重（如文件完整性校验）；不要用于密码或安全签名。
 * - SHA-256/512：不可逆摘要，适合完整性校验；如需身份校验请改用 HMAC/数字签名。
 * - HMAC-SHA256/512：基于共享密钥的认证摘要，适合接口签名、Webhook 校验、消息防篡改；不具备第三方可验证的不可否认性。
 * - AES：用于加密机密数据，优先使用 GCM 等 AEAD 模式（兼具机密性与完整性）；CBC 为兼容场景。
 * - BCrypt：用于密码/口令存储的哈希，避免明文或直接使用 MD5/SHA。
 * - 密码存储：请使用 BCrypt（本工具已提供）或专用口令哈希，避免直接用 MD5/SHA/HMAC。
 *
 * @author wangweijun
 * @version v1.0
 * @since 2026-01-23 10:17:09
 */
public class EncryptUtils extends AbstractUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * AES 加密工具类（机密数据加密）
     * <p>
     * 核心用途：加密需要保密的数据（比如用户手机号、身份证号、敏感配置），优先用 GCM 模式（兼具加密和完整性校验）<br/>
     * 例子：将用户的敏感手机号加密后存储到数据库，读取时再解密，防止数据库泄露导致敏感信息暴露。
     * </p>
     */
    public static class AES extends AbstractUtils {

        // AES constants
        private static final String AES = "AES";
        private static final String AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";
        private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
        private static final int GCM_TAG_LENGTH_BITS = 128; // 16 bytes tag
        private static final int GCM_IV_LENGTH_BYTES = 12;  // recommended 12 bytes
        private static final int CBC_IV_LENGTH_BYTES = 16;  // 16 bytes for AES block size

        /**
         * 使用带有 HMAC-SHA256 的 PBKDF2 从密码短语派生 AES 密钥
         *
         * @param passphrase 密码短语
         * @param salt       盐（如果为 null，则将生成一个随机盐）
         * @param keyBits    以位为单位的密钥大小（128、192 或 256）
         * @param iterations 迭代次数（如果小于等于 0，则默认值为 65536）
         *
         * @return derived AES key bytes
         */
        public static byte[] deriveKey(String passphrase, byte[] salt, int keyBits, int iterations) {
            if (passphrase == null) {
                throw new IllegalArgumentException("passphrase must not be null");
            }
            if (salt == null) {
                // generate random salt if not provided
                salt = randomBytes(16);
            }
            if (keyBits != 128 && keyBits != 192 && keyBits != 256) {
                throw new IllegalArgumentException("keyBits must be 128, 192, or 256");
            }
            if (iterations <= 0) {
                iterations = 65536;
            }
            try {
                KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, iterations, keyBits);
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                return factory.generateSecret(spec).getEncoded();
            } catch (Exception e) {
                throw new EncryptException("Failed to derive AES key", e);
            }
        }

        /**
         * AES-GCM 加密
         *
         * @param plainText 明文
         * @param key       密钥
         *
         * @return 密文
         */
        public static String encryptGCM(String plainText, byte[] key) {
            byte[] iv = randomBytes(GCM_IV_LENGTH_BYTES);
            byte[] cipher = encryptGCM(plainText.getBytes(StandardCharsets.UTF_8), key, iv);
            // pack iv + cipher into single array
            byte[] packed = ByteBuffer.allocate(iv.length + cipher.length)
                    .put(iv)
                    .put(cipher)
                    .array();
            return Base64.getEncoder().encodeToString(packed);
        }

        /**
         * AES-GCM 解密
         *
         * @param base64IvCipher base64 编码的 iv + 密文
         * @param key            密钥
         *
         * @return 明文
         */
        public static String decryptGCM(String base64IvCipher, byte[] key) {
            byte[] packed = Base64.getDecoder().decode(base64IvCipher);
            if (packed.length < GCM_IV_LENGTH_BYTES + 1) {
                throw new IllegalArgumentException("Invalid GCM payload");
            }
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            byte[] cipher = new byte[packed.length - GCM_IV_LENGTH_BYTES];
            System.arraycopy(packed, 0, iv, 0, iv.length);
            System.arraycopy(packed, iv.length, cipher, 0, cipher.length);
            byte[] plain = decryptGCM(cipher, key, iv);
            return new String(plain, StandardCharsets.UTF_8);
        }

        /**
         * AES-GCM 加密
         *
         * @param plain 明文
         * @param key   密钥
         * @param iv    初始化向量
         *
         * @return 密文
         */
        public static byte[] encryptGCM(byte[] plain, byte[] key, byte[] iv) {
            try {
                Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
                GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
                SecretKeySpec keySpec = new SecretKeySpec(key, AES);
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
                return cipher.doFinal(plain);
            } catch (Exception e) {
                throw new EncryptException("AES-GCM encrypt failed", e);
            }
        }

        /**
         * AES-GCM 解密
         *
         * @param cipherText 密文
         * @param key        密钥
         * @param iv         初始化向量
         *
         * @return 明文
         */
        public static byte[] decryptGCM(byte[] cipherText, byte[] key, byte[] iv) {
            try {
                Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
                GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
                SecretKeySpec keySpec = new SecretKeySpec(key, AES);
                cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
                return cipher.doFinal(cipherText);
            } catch (Exception e) {
                throw new DecryptException("AES-GCM decrypt failed", e);
            }
        }

        /**
         * AES-CBC 加密
         *
         * @param plainText 明文
         * @param key       密钥
         *
         * @return 密文
         */
        public static String encryptCBC(String plainText, byte[] key) {
            byte[] iv = randomBytes(CBC_IV_LENGTH_BYTES);
            byte[] cipher = encryptCBC(plainText.getBytes(StandardCharsets.UTF_8), key, iv);
            byte[] packed = ByteBuffer.allocate(iv.length + cipher.length)
                    .put(iv)
                    .put(cipher)
                    .array();
            return Base64.getEncoder().encodeToString(packed);
        }

        /**
         * AES-CBC 解密
         *
         * @param base64IvCipher base64 编码的 iv + 密文
         * @param key            密钥
         *
         * @return 明文
         */
        public static String decryptCBC(String base64IvCipher, byte[] key) {
            byte[] packed = Base64.getDecoder().decode(base64IvCipher);
            if (packed.length < CBC_IV_LENGTH_BYTES + 1) {
                throw new IllegalArgumentException("Invalid CBC payload");
            }
            byte[] iv = new byte[CBC_IV_LENGTH_BYTES];
            byte[] cipher = new byte[packed.length - CBC_IV_LENGTH_BYTES];
            System.arraycopy(packed, 0, iv, 0, iv.length);
            System.arraycopy(packed, iv.length, cipher, 0, cipher.length);
            byte[] plain = decryptCBC(cipher, key, iv);
            return new String(plain, StandardCharsets.UTF_8);
        }

        /**
         * AES-CBC 加密
         *
         * @param plain 明文
         * @param key   密钥
         * @param iv    初始化向量
         *
         * @return 密文
         */
        public static byte[] encryptCBC(byte[] plain, byte[] key, byte[] iv) {
            try {
                Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5);
                SecretKeySpec keySpec = new SecretKeySpec(key, AES);
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
                return cipher.doFinal(plain);
            } catch (Exception e) {
                throw new EncryptException("AES-CBC encrypt failed", e);
            }
        }

        /**
         * AES-CBC 解密
         *
         * @param cipherText 密文
         * @param key        密钥
         * @param iv         初始化向量
         *
         * @return 明文
         */
        public static byte[] decryptCBC(byte[] cipherText, byte[] key, byte[] iv) {
            try {
                Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5);
                SecretKeySpec keySpec = new SecretKeySpec(key, AES);
                cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
                return cipher.doFinal(cipherText);
            } catch (Exception e) {
                throw new DecryptException("AES-CBC decrypt failed", e);
            }
        }

        /**
         * 生成随机字节数组
         *
         * @param size 大小
         *
         * @return 随机字节数组
         */
        public static byte[] randomBytes(int size) {
            byte[] bytes = new byte[size];
            SECURE_RANDOM.nextBytes(bytes);
            return bytes;
        }
    }

    /**
     * BCrypt 加密工具类（密码存储专用哈希）
     * <p>
     * 用户密码存储（自动加盐、慢哈希，防暴力破解），绝对不能用 MD5/SHA 直接存密码。<br/>
     * 例子：用户注册时，将密码用 BCrypt 哈希后存储到数据库；登录时，将用户输入的密码哈希后和数据库中的哈希值对比，验证密码是否正确。
     * </p>
     */
    public static class BCrypt extends AbstractUtils {

        // jBCrypt cost range usually 4-31
        private static final int DEFAULT_COST = 10;


        /**
         * 对明文进行 BCrypt 哈希加密，使用默认成本值
         *
         * @param plainText 明文
         *
         * @return 哈希值
         */
        public static String hash(String plainText) {
            return hash(plainText, DEFAULT_COST);
        }

        /**
         * 对明文进行 BCrypt 哈希加密，使用指定成本值
         *
         * @param plainText 明文
         * @param cost      成本值（4-31），如果不在范围内则使用默认值
         *
         * @return 哈希值
         */
        public static String hash(String plainText, int cost) {
            if (plainText == null) {
                throw new IllegalArgumentException("plainText must not be null");
            }
            if (cost < 4 || cost > 31) {
                cost = DEFAULT_COST;
            }
            String salt = org.mindrot.jbcrypt.BCrypt.gensalt(cost, SECURE_RANDOM);
            return org.mindrot.jbcrypt.BCrypt.hashpw(plainText, salt);
        }

        /**
         * 验证明文与哈希值是否匹配
         *
         * @param plainText 明文
         * @param hashed    哈希值
         *
         * @return 是否匹配
         */
        public static boolean verify(String plainText, String hashed) {
            if (plainText == null || hashed == null) {
                return false;
            }
            try {
                return org.mindrot.jbcrypt.BCrypt.checkpw(plainText, hashed);
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * MD5 加密工具类
     * <p>
     * 非安全场景的校验 / 去重<br/>
     * 核心用途：文件完整性校验、数据去重（比如判断两个文件是否相同），绝对不能用于密码 / 安全场景<br/>
     * 例子：下载文件后，用 MD5 校验文件是否和服务器提供的 MD5 值一致，确认文件没被篡改 / 损坏。
     * </p>
     */
    public static class MD5 extends AbstractUtils {

        /**
         * 获取字符串的 MD5 哈希值
         *
         * @param input 输入字符串
         *
         * @return MD5 哈希值的十六进制表示
         */
        public static String hex(String input) {
            if (input == null) {
                throw new IllegalArgumentException("input must not be null");
            }
            return hex(input.getBytes(StandardCharsets.UTF_8));
        }

        /**
         * 获取字节数组的 MD5 哈希值
         *
         * @param data 输入字节数组
         *
         * @return MD5 哈希值的十六进制表示
         */
        public static String hex(byte[] data) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] digest = md.digest(data);
                return toHex(digest);
            } catch (Exception e) {
                throw new EncryptException("MD5 digest failed", e);
            }
        }

        /**
         * 将字节数组转换为十六进制字符串
         *
         * @param bytes 输入字节数组
         *
         * @return 十六进制字符串
         */
        private static String toHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                String s = Integer.toHexString(b & 0xff);
                if (s.length() == 1) sb.append('0');
                sb.append(s);
            }
            return sb.toString();
        }

        /**
         * 获取文件的 MD5 哈希值
         *
         * @param file 输入文件
         *
         * @return MD5 哈希值的十六进制表示
         */
        public static String hex(File file) {
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        md5.update(buffer, 0, len);
                    }
                }
                return toHex(md5.digest());
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new EncryptException("MD5 file digest failed", e);
            }
        }

        /**
         * 获取文件的 MD5 哈希值
         *
         * @param path 输入文件路径
         *
         * @return MD5 哈希值的十六进制表示
         */
        public static String hex(Path path) {
            return hex(path.toFile());
        }
    }

    /**
     * SHA 哈希工具类
     * <p>
     * 无需密钥，适用于不可逆摘要与完整性校验（不提供身份保证），SHA-256/512：不可逆摘要（比 MD5 安全）<br/>
     * 核心用途：数据完整性校验（比 MD5 更安全），比如软件安装包的校验、数据去重。<br/>
     * 例子：区块链中用 SHA-256 生成区块哈希，确保区块数据不可篡改。
     * </p>
     */
    public static class SHA extends AbstractUtils {

        private static final String SHA_256 = "SHA-256";
        private static final String SHA_512 = "SHA-512";

        /**
         * 计算字符串的 SHA-256 十六进制摘要
         *
         * @param input 输入字符串
         *
         * @return 十六进制摘要
         */
        public static String hex256(String input) {
            if (input == null) {
                throw new IllegalArgumentException("input must not be null");
            }
            return hex256(input.getBytes(StandardCharsets.UTF_8));
        }

        /**
         * 计算字节数组的 SHA-256 十六进制摘要
         *
         * @param data 输入字节数组
         *
         * @return 十六进制摘要
         */
        public static String hex256(byte[] data) {
            return digestToHex(SHA_256, data);
        }

        /**
         * 计算字符串的 SHA-512 十六进制摘要
         *
         * @param input 输入字符串
         *
         * @return 十六进制摘要
         */
        public static String hex512(String input) {
            if (input == null) {
                throw new IllegalArgumentException("input must not be null");
            }
            return hex512(input.getBytes(StandardCharsets.UTF_8));
        }

        /**
         * 计算字节数组的 SHA-512 十六进制摘要
         *
         * @param data 输入字节数组
         *
         * @return 十六进制摘要
         */
        public static String hex512(byte[] data) {
            return digestToHex(SHA_512, data);
        }

        /**
         * 计算文件的 SHA 摘要（可选 SHA-256 或 SHA-512）
         *
         * @param file   输入文件
         * @param use512 是否使用 SHA-512（否则使用 SHA-256）
         */
        public static String hex(File file, boolean use512) {
            String algorithm = use512 ? SHA_512 : SHA_256;
            try {
                MessageDigest digest = MessageDigest.getInstance(algorithm);
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        digest.update(buffer, 0, len);
                    }
                }
                return toHex(digest.digest());
            } catch (Exception e) {
                throw new EncryptException("SHA file digest failed", e);
            }
        }

        /**
         * 计算文件路径的 SHA 摘要（可选 SHA-256 或 SHA-512）
         *
         * @param path   输入文件路径
         * @param use512 是否使用 SHA-512（否则使用 SHA-256）
         */
        public static String hex(Path path, boolean use512) {
            return hex(path.toFile(), use512);
        }

        /**
         * 计算指定算法的 SHA 十六进制摘要
         *
         * @param algorithm SHA 算法名称，如 SHA-256 或 SHA-512
         * @param data      输入字节数组
         *
         * @return 十六进制摘要
         */
        private static String digestToHex(String algorithm, byte[] data) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithm);
                return toHex(md.digest(data));
            } catch (Exception e) {
                throw new EncryptException("SHA digest failed", e);
            }
        }

        /**
         * 将字节数组转换为十六进制字符串
         *
         * @param bytes 输入字节数组
         *
         * @return 十六进制字符串
         */
        private static String toHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                String s = Integer.toHexString(b & 0xff);
                if (s.length() == 1) sb.append('0');
                sb.append(s);
            }
            return sb.toString();
        }
    }

    /**
     * HMAC 摘要工具类
     * <p>
     * 基于密钥的消息认证，适合完整性与身份校验（常用于签名 / 防篡改）<br/>
     * 核心用途：接口签名、Webhook 校验、消息防篡改（防止数据被篡改 + 验证发送方身份）<br/>
     * 例子：前后端接口通信时，后端给前端一个密钥，前端用 HMAC-SHA256 对请求参数 + 密钥生成签名，后端验证签名是否一致，确保请求是合法前端发送的、参数没被篡改。
     * </p>
     */
    public static class HMAC extends AbstractUtils {

        private static final String HMAC_SHA256 = "HmacSHA256";
        private static final String HMAC_SHA512 = "HmacSHA512";

        /**
         * 计算字符串的 HmacSHA256 十六进制摘要
         *
         * @param key  密钥
         * @param data 数据
         *
         * @return 十六进制摘要
         */
        public static String sha256Hex(byte[] key, String data) {
            if (data == null) {
                throw new IllegalArgumentException("data must not be null");
            }
            return sha256Hex(key, data.getBytes(StandardCharsets.UTF_8));
        }

        /**
         * 计算字节数组的 HmacSHA256 十六进制摘要
         *
         * @param key  密钥
         * @param data 数据
         *
         * @return 十六进制摘要
         */
        public static String sha256Hex(byte[] key, byte[] data) {
            return hmacToHex(HMAC_SHA256, key, data);
        }

        /**
         * 计算字符串的 HmacSHA512 十六进制摘要
         *
         * @param key  密钥
         * @param data 数据
         *
         * @return 十六进制摘要
         */
        public static String sha512Hex(byte[] key, String data) {
            if (data == null) {
                throw new IllegalArgumentException("data must not be null");
            }
            return sha512Hex(key, data.getBytes(StandardCharsets.UTF_8));
        }

        /**
         * 计算字节数组的 HmacSHA512 十六进制摘要
         *
         * @param key  密钥
         * @param data 数据
         *
         * @return 十六进制摘要
         */
        public static String sha512Hex(byte[] key, byte[] data) {
            return hmacToHex(HMAC_SHA512, key, data);
        }

        /**
         * 计算指定算法的 HMAC 十六进制摘要
         *
         * @param algorithm HMAC 算法名称，如 HmacSHA256 或 HmacSHA512
         * @param key       密钥
         * @param data      数据
         *
         * @return 十六进制摘要
         */
        private static String hmacToHex(String algorithm, byte[] key, byte[] data) {
            if (key == null) {
                throw new IllegalArgumentException("key must not be null");
            }
            try {
                Mac mac = Mac.getInstance(algorithm);
                mac.init(new SecretKeySpec(key, algorithm));
                return toHex(mac.doFinal(data));
            } catch (Exception e) {
                throw new EncryptException("HMAC digest failed", e);
            }
        }

        /**
         * 将字节数组转换为十六进制字符串
         *
         * @param bytes 输入字节数组
         *
         * @return 十六进制字符串
         */
        private static String toHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                String s = Integer.toHexString(b & 0xff);
                if (s.length() == 1) sb.append('0');
                sb.append(s);
            }
            return sb.toString();
        }
    }

}
