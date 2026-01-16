package io.github.thebesteric.framework.agile.commons.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成、解析和验证 JWT Token
 */
public class JWTUtils {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    /** JWT 密钥 - 从配置文件中读取，如果没有配置则使用默认值 */
    private final String secret;

    /** JWT 过期时间（毫秒） */
    private final Long expiration;


    public JWTUtils(String secret, Long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param subject 主题（通常是用户ID或用户名）
     *
     * @return JWT Token
     */
    public String generateToken(String subject) {
        return generateToken(subject, null);
    }

    /**
     * 生成 JWT Token（带自定义声明）
     *
     * @param subject 主题（通常是用户ID或用户名）
     * @param claims  自定义声明
     *
     * @return JWT Token
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        return generateToken(subject, claims, expiration);
    }

    /**
     * 生成 JWT Token（自定义过期时间）
     *
     * @param subject          主题
     * @param claims           自定义声明
     * @param expirationMillis 过期时间（毫秒）
     *
     * @return JWT Token
     */
    public String generateToken(String subject, Map<String, Object> claims, Long expirationMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        JwtBuilder builder = Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey());

        if (claims != null && !claims.isEmpty()) {
            builder.claims(claims);
        }

        return builder.compact();
    }

    /**
     * 解析 JWT Token
     *
     * @param token JWT Token
     *
     * @return Claims 对象
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            loggerPrinter.error("JWT 解析失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 解析 JWT Token（不验证签名）
     * 用于解析不是由当前服务生成的 Token，只解析载荷内容，不验证签名
     *
     * @param token JWT Token
     *
     * @return Claims 对象
     */
    public static Claims parseTokenWithoutVerify(String token) {
        try {
            // 分割 JWT token，格式为 header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new MalformedJwtException("Invalid JWT token format");
            }

            // 获取 payload 部分
            String payload = parts[1];

            // 构造一个无签名的 JWT 格式: header.payload.
            // 使用 "none" 算法的 header
            String unsecuredHeader = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
            String unsecuredJwt = unsecuredHeader + "." + payload + ".";

            // 使用 unsecured parser 解析
            return Jwts.parser()
                    .unsecured()
                    .build()
                    .parseUnsecuredClaims(unsecuredJwt)
                    .getPayload();
        } catch (Exception e) {
            loggerPrinter.error("JWT 解析失败（无签名验证）: {}", e.getMessage());
            throw new MalformedJwtException("Failed to parse JWT token without verification", e);
        }
    }

    /**
     * 从 Token 中获取主题（Subject）
     *
     * @param token JWT Token
     *
     * @return 主题内容
     */
    public String getSubject(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从 Token 中获取指定的声明
     *
     * @param token JWT Token
     * @param key   声明的键
     *
     * @return 声明的值
     */
    public Object getClaim(String token, String key) {
        return parseToken(token).get(key);
    }

    /**
     * 从 Token 中获取指定的声明（指定类型）
     *
     * @param token JWT Token
     * @param key   声明的键
     * @param clazz 值的类型
     *
     * @return 声明的值
     */
    public <T> T getClaim(String token, String key, Class<T> clazz) {
        return parseToken(token).get(key, clazz);
    }

    /**
     * 获取 Token 的过期时间
     *
     * @param token JWT Token
     *
     * @return 过期时间
     */
    public Date getExpiration(String token) {
        return parseToken(token).getExpiration();
    }

    /**
     * 获取 Token 的签发时间
     *
     * @param token JWT Token
     *
     * @return 签发时间
     */
    public Date getIssuedAt(String token) {
        return parseToken(token).getIssuedAt();
    }

    /**
     * 验证 Token 是否过期
     *
     * @param token JWT Token
     *
     * @return true=已过期，false=未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = getExpiration(token);
            return expirationDate.before(new Date());
        } catch (JwtException e) {
            loggerPrinter.error("Token 过期验证失败: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     *
     * @return true=有效，false=无效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            loggerPrinter.error("JWT 签名验证失败: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            loggerPrinter.error("JWT 格式错误: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            loggerPrinter.error("JWT 已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            loggerPrinter.error("不支持的 JWT: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            loggerPrinter.error("JWT 参数为空: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 验证 Token 是否有效（针对特定主题）
     *
     * @param token   JWT Token
     * @param subject 期望的主题
     *
     * @return true=有效，false=无效
     */
    public boolean validateToken(String token, String subject) {
        try {
            String tokenSubject = getSubject(token);
            return subject.equals(tokenSubject) && !isTokenExpired(token);
        } catch (JwtException e) {
            loggerPrinter.error("Token 验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 刷新 Token（保持原有的声明，重新生成过期时间）
     *
     * @param token 原 Token
     *
     * @return 新的 Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            String subject = claims.getSubject();

            // 创建新的 Map 来存储自定义声明（排除标准声明）
            Map<String, Object> customClaims = new java.util.HashMap<>();
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                String key = entry.getKey();
                // 排除标准的 JWT 声明
                if (!key.equals(Claims.SUBJECT) &&
                    !key.equals(Claims.ISSUED_AT) &&
                    !key.equals(Claims.EXPIRATION) &&
                    !key.equals(Claims.ID) &&
                    !key.equals(Claims.ISSUER) &&
                    !key.equals(Claims.AUDIENCE) &&
                    !key.equals(Claims.NOT_BEFORE)) {
                    customClaims.put(key, entry.getValue());
                }
            }

            return generateToken(subject, customClaims);
        } catch (JwtException e) {
            loggerPrinter.error("Token 刷新失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 获取 Token 的剩余有效时间（毫秒）
     *
     * @param token JWT Token
     *
     * @return 剩余有效时间（毫秒），如果已过期返回 0
     */
    public long getRemainingTime(String token) {
        try {
            Date expirationDate = getExpiration(token);
            long remaining = expirationDate.getTime() - System.currentTimeMillis();
            return Math.max(remaining, 0);
        } catch (JwtException e) {
            loggerPrinter.error("获取剩余时间失败: {}", e.getMessage());
            return 0;
        }
    }
}
