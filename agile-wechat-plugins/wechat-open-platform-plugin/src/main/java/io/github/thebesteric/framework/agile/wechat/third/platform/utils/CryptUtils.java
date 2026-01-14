package io.github.thebesteric.framework.agile.wechat.third.platform.utils;

import io.github.thebesteric.framework.agile.commons.util.AbstractUtils;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.wechat.third.platform.config.mini.WechatMiniProperties;
import io.github.thebesteric.framework.agile.wechat.third.platform.config.third.WechatThirdPlatformProperties;
import io.github.thebesteric.framework.agile.wechat.third.platform.exception.AesException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * SignatureUtils
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-29 11:21:37
 */
@Slf4j
public class CryptUtils {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();
    private static final Charset UTF_8_CHARSET = StandardCharsets.UTF_8;

    private final String appId;
    private final String verifyToken;
    private final byte[] aesKey;

    public CryptUtils(WechatThirdPlatformProperties properties) throws AesException {
        this(properties.getComponentAppId(), properties.getVerifyToken(), properties.getEncryptAesKey());
    }

    public CryptUtils(WechatMiniProperties properties) throws AesException {
        this(properties.getAppId(), properties.getMessagePush().getToken(), properties.getMessagePush().getEncodingAesKey());
    }

    public CryptUtils(String appId, String verifyToken, String encryptAesKey) throws AesException {
        if (encryptAesKey.length() != 43) {
            throw new AesException(AesException.IllegalAesKey);
        }
        this.verifyToken = verifyToken;
        this.appId = appId;
        aesKey = Base64.decodeBase64(encryptAesKey + "=");
    }

    /**
     * verifySignature
     *
     * @param signature 签名
     * @param timestamp 时间戳
     * @param nonce     随机字符串
     * @param encrypt   加密数据
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/7/29 11:23
     */
    public boolean verifySignature(String signature, String timestamp, String nonce, String encrypt) {
        String[] arr = {verifyToken, timestamp, nonce, encrypt};
        // 进行字典序排序
        Arrays.sort(arr);
        StringBuilder sb = new StringBuilder();
        for (String str : arr) {
            sb.append(str);
        }
        // 将时间戳、随机字符串、加密数据和令牌按照字典序排序拼接成一个字符串
        String tempStr = sb.toString();

        // 使用 SHA1 算法对拼接后的字符串进行加密
        String encryptedStr = sha1(tempStr);

        // 将生成的签名与微信服务器发送的签名进行对比
        return encryptedStr != null && encryptedStr.equals(signature);
    }

    /**
     * 对密文进行解密.
     *
     * @param encrypt 需要解密的密文
     *
     * @return 解密得到的明文
     *
     * @throws AesException aes解密失败
     */
    public String decrypt(String encrypt) throws AesException {
        byte[] original;
        try {
            // 设置解密模式为AES的CBC模式
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(aesKey, 0, 16));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);

            // 使用BASE64对密文进行解码
            byte[] encrypted = Base64.decodeBase64(encrypt);

            // 解密
            original = cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new AesException(AesException.DecryptAESError);
        }

        String xmlContent, from_appid;
        try {
            // 去除补位字符
            byte[] bytes = PKCS7Encoder.decode(original);

            // 分离16位随机字符串,网络字节序和AppId
            byte[] networkOrder = Arrays.copyOfRange(bytes, 16, 20);

            int xmlLength = recoverNetworkBytesOrder(networkOrder);

            xmlContent = new String(Arrays.copyOfRange(bytes, 20, 20 + xmlLength), UTF_8_CHARSET);
            from_appid = new String(Arrays.copyOfRange(bytes, 20 + xmlLength, bytes.length), UTF_8_CHARSET);
        } catch (Exception e) {
            throw new AesException(AesException.IllegalBuffer);
        }

        // appid不相同的情况
        if (!from_appid.equals(appId)) {
            throw new AesException(AesException.ValidateAppidError);
        }
        return xmlContent;
    }

    /**
     * sha1 加密
     *
     * @param str 待加密字符串
     *
     * @return java.lang.String
     *
     * @author wangweijun
     * @since 2024/7/29 11:36
     */
    public static String sha1(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(str.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    sb.append("0");
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            loggerPrinter.error("sha1加密失败: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * sha256 加密
     *
     * @param str    待加密字符串
     * @param secret 加密密钥
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/8/5 11:51
     */
    public static String sha256(String str, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] bytes = sha256Hmac.doFinal(str.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            loggerPrinter.error("sha256加密失败: {}", e.getMessage(), e);
        }
        return null;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 还原4个字节的网络字节序
     *
     * @param orderBytes
     *
     * @return int
     *
     * @author wangweijun
     * @since 2024/7/29 18:53
     */
    int recoverNetworkBytesOrder(byte[] orderBytes) {
        int sourceNumber = 0;
        for (int i = 0; i < 4; i++) {
            sourceNumber <<= 8;
            sourceNumber |= orderBytes[i] & 0xff;
        }
        return sourceNumber;
    }

    /**
     * 提供基于PKCS7算法的加解密接口.
     */
    static class PKCS7Encoder extends AbstractUtils {

        public static final int BLOCK_SIZE = 32;

        /**
         * 获得对明文进行补位填充的字节.
         *
         * @param count 需要进行填充补位操作的明文字节个数
         *
         * @return 补齐用的字节数组
         */
        static byte[] encode(int count) {
            // 计算需要填充的位数
            int amountToPad = BLOCK_SIZE - (count % BLOCK_SIZE);
            if (amountToPad == 0) {
                amountToPad = BLOCK_SIZE;
            }
            // 获得补位所用的字符
            char padChr = chr(amountToPad);
            String tmp = new String();
            for (int index = 0; index < amountToPad; index++) {
                tmp += padChr;
            }
            return tmp.getBytes(UTF_8_CHARSET);
        }

        /**
         * 删除解密后明文的补位字符
         *
         * @param decrypted 解密后的明文
         *
         * @return 删除补位字符后的明文
         */
        static byte[] decode(byte[] decrypted) {
            int pad = decrypted[decrypted.length - 1];
            if (pad < 1 || pad > 32) {
                pad = 0;
            }
            return Arrays.copyOfRange(decrypted, 0, decrypted.length - pad);
        }

        /**
         * 将数字转化成ASCII码对应的字符，用于对明文进行补码
         *
         * @param n 需要转化的数字
         *
         * @return 转化得到的字符
         */
        static char chr(int n) {
            byte target = (byte) (n & 0xFF);
            return (char) target;
        }

    }
}
