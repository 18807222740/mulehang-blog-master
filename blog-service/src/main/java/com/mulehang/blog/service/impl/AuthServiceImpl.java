package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.dto.LoginRequest;
import com.mulehang.blog.dto.RegisterRequest;
import com.mulehang.blog.entity.SysUser;
import com.mulehang.blog.entity.SysUserRole;
import com.mulehang.blog.exception.BusinessException;
import com.mulehang.blog.mapper.SysRoleMapper;
import com.mulehang.blog.mapper.SysUserMapper;
import com.mulehang.blog.mapper.SysUserRoleMapper;
import com.mulehang.blog.service.AuthService;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mulehang.blog.security.TokenBlacklistService;
import com.mulehang.blog.util.JwtUtil;
import com.mulehang.blog.util.PasswordUtil;
import com.mulehang.blog.vo.LoginResponse;
import com.mulehang.blog.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 认证服务实现类。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final JwtUtil jwtUtil;
    private final PasswordUtil passwordUtil;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 用户登录。
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));

        if (user == null) {
            throw BusinessException.badRequest("用户名或密码错误");
        }

        if (!passwordUtil.matches(request.getPassword(), user.getPasswordSalt(), user.getPasswordHash())) {
            throw BusinessException.badRequest("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw BusinessException.forbidden("账号已被禁用");
        }

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        List<String> roles = userRoleMapper.selectRoleCodesByUserId(user.getId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);

        UserInfoVO userInfo = UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .profile(user.getProfile())
                .roles(roles)
                .lastLoginTime(user.getLastLoginTime())
                .build();

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .userInfo(userInfo)
                .build();
    }

    /**
     * 用户注册。
     *
     * @param request 注册请求
     * @return 登录响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw BusinessException.badRequest("用户名已存在");
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, request.getEmail()));
            if (count > 0) {
                throw BusinessException.badRequest("邮箱已被注册");
            }
        }

        Long defaultRoleId = roleMapper.selectIdByCode("USER");
        if (defaultRoleId == null) {
            throw BusinessException.internalError("默认角色未配置: USER");
        }

        String salt = passwordUtil.generateSalt();
        String passwordHash = passwordUtil.encryptPassword(request.getPassword(), salt);

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordHash);
        user.setPasswordSalt(salt);
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        userMapper.insert(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(defaultRoleId);
        userRoleMapper.insert(userRole);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(request.getUsername());
        loginRequest.setPassword(request.getPassword());
        return login(loginRequest);
    }

    /**
     * 退出登录。
     *
     * @param userId 用户 ID
     * @param token  当前 Token
     */
    @Override
    public void logout(Long userId, String token) {
        if (StringUtils.hasText(token)) {
            try {
                DecodedJWT jwt = jwtUtil.verifyToken(token);
                String jti = jwt.getId();
                Date expiresAt = jwt.getExpiresAt();
                long ttlSeconds = expiresAt == null ? 0L
                        : Math.max(0L, (expiresAt.getTime() - System.currentTimeMillis()) / 1000);
                tokenBlacklistService.blacklist(jti, ttlSeconds);
            } catch (Exception e) {
                log.warn("注销时 Token 处理失败: {}", e.getMessage());
            }
        }
        log.info("用户 {} 退出登录", userId);
    }
}
