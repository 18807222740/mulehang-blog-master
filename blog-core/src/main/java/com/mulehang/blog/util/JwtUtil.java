package com.mulehang.blog.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT 工具类
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * -- GETTER --
     *  获取 Token 过期时间（秒）
     */
    @Getter
    @Value("${jwt.expiration:86400}")
    private Long expiration; // 默认 24 小时（秒）

    @Value("${jwt.issuer:mulehang-blog}")
    private String issuer;

    /**
     * 校验 JWT 密钥强度。
     */
    @PostConstruct
    public void validateSecret() {
        if (!StringUtils.hasText(secret) || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET 未配置或长度不足（至少 32 位）");
        }
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param roles    角色列表
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, List<String> roles) {
        return generateToken(userId, username, roles, expiration);
    }

    /**
     * 生成 JWT Token（自定义过期时间）
     *
     * @param userId         用户 ID
     * @param username       用户名
     * @param roles          角色列表
     * @param expirationSecs 过期时间（秒）
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, List<String> roles, Long expirationSecs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationSecs * 1000);

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(username)
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("roles", roles)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * 验证并解析 Token
     *
     * @param token JWT Token
     * @return 解析后的 JWT
     */
    public DecodedJWT verifyToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .build()
                    .verify(token);
        } catch (JWTVerificationException e) {
            log.error("JWT 验证失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 从 Token 中获取用户ID
     *
     * @param token JWT Token
     * @return 用户 ID
     */
    public Long getUserIdFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("userId").asLong();
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("username").asString();
    }

    /**
     * 从 Token 中获取角色列表
     *
     * @param token JWT Token
     * @return 角色列表
     */
    public List<String> getRolesFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("roles").asList(String.class);
    }

    /**
     * 从 Token 中获取 jti
     *
     * @param token JWT Token
     * @return jti
     */
    public String getTokenIdFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getId();
    }

    /**
     * 检查 Token 是否过期
     *
     * @param token JWT Token
     * @return true-已过期，false-未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (JWTVerificationException e) {
            return true;
        }
    }

}
