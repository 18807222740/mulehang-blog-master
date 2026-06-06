package com.mulehang.blog.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.entity.SysRole;
import com.mulehang.blog.entity.SysUser;
import com.mulehang.blog.entity.SysUserRole;
import com.mulehang.blog.mapper.SysRoleMapper;
import com.mulehang.blog.mapper.SysUserMapper;
import com.mulehang.blog.mapper.SysUserRoleMapper;
import com.mulehang.blog.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 用户数据初始化组件
 * 在应用启动时检查并创建默认管理员用户
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class UserDataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordUtil passwordUtil;
    @Value("${blog.admin.init-password:}")
    private String initPassword;

    @Override
    public void run(String... args) {
        log.info("开始检查用户数据初始化...");

        // 检查是否已存在管理员用户
        Long adminCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "admin"));

        if (adminCount > 0) {
            log.info("管理员用户已存在，跳过初始化");
            return;
        }

        // 创建默认管理员用户
        createDefaultAdmin();
        log.info("用户数据初始化完成");
    }

    /**
     * 创建默认管理员用户
     */
    private void createDefaultAdmin() {
        if (!StringUtils.hasText(initPassword)) {
            log.warn("未配置初始化管理员密码（blog.admin.init-password），跳过默认管理员创建");
            return;
        }
        String rawPassword = initPassword.trim();
        String salt = passwordUtil.generateSalt();
        String passwordHash = passwordUtil.encryptPassword(rawPassword, salt);

        // 创建管理员用户
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordHash);
        admin.setPasswordSalt(salt);
        admin.setNickname("管理员");
        admin.setEmail("admin@mulehang.com");
        admin.setStatus(1);
        userMapper.insert(admin);

        log.info("创建默认管理员用户成功，用户名：admin（请尽快修改密码）");

        // 查询管理员角色
        SysRole adminRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, "ADMIN"));

        if (adminRole != null) {
            // 分配管理员角色
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(admin.getId());
            userRole.setRoleId(adminRole.getId());
            userRoleMapper.insert(userRole);
            log.info("为管理员用户分配角色成功");
        }
    }
}
