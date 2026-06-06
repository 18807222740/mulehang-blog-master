package com.mulehang.blog.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加密工具类
 */
@Component
public class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成随机盐
     * @return Base64 编码的盐
     */
    public String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 加密密码（使用 BCrypt + 自定义盐）
     * @param rawPassword 原始密码
     * @param salt        盐
     * @return 加密后的密码
     */
    public String encryptPassword(String rawPassword, String salt) {
        String saltedPassword = rawPassword + salt;
        return ENCODER.encode(saltedPassword);
    }

    /**
     * 验证密码
     * @param rawPassword     原始密码
     * @param salt            盐
     * @param encodedPassword 加密后的密码
     * @return true-匹配，false-不匹配
     */
    public boolean matches(String rawPassword, String salt, String encodedPassword) {
        String saltedPassword = rawPassword + salt;
        return ENCODER.matches(saltedPassword, encodedPassword);
    }
}

