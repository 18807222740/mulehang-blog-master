package com.mulehang.blog.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具（仅用于开发测试）
 * 用于生成初始化数据中的密码哈希
 */
public class PasswordGenerator {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static void main(String[] args) {
        // 测试密码
        String rawPassword = "mulehang";
        String saltBase64 = "REPLACE_WITH_BASE64_SALT";

        // 生成密码哈希
        String saltedPassword = rawPassword + saltBase64;
        String passwordHash = ENCODER.encode(saltedPassword);

        System.out.println("原始密码: " + rawPassword);
        System.out.println("盐值(Base64): " + saltBase64);
        System.out.println("密码哈希: " + passwordHash);

        // 验证
        boolean matches = ENCODER.matches(saltedPassword, passwordHash);
        System.out.println("验证结果: " + matches);
    }
}
