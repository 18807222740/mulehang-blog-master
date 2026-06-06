package com.mulehang.blog.service.impl;

import com.mulehang.blog.entity.SysUser;
import com.mulehang.blog.mapper.SysUserMapper;
import com.mulehang.blog.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 自定义用户详情服务
 * 用于 Spring Security 从数据库加载用户信息
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    /**
     * 根据用户 ID 加载用户详情
     *
     * @param userId 用户 ID（字符串形式）
     * @return Spring Security 的 UserDetails
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        SysUser user = userMapper.selectById(Long.parseLong(userId));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }

        // 查询用户角色
        List<String> roles = userRoleMapper.selectRoleCodesByUserId(user.getId());
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

        return new User(user.getUsername(), user.getPasswordHash(), authorities);
    }
}
