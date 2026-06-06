package com.mulehang.blog.service.impl;

import com.mulehang.blog.entity.SysUser;
import com.mulehang.blog.mapper.SysUserMapper;
import com.mulehang.blog.security.UserStatusCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户启用状态检查（带本地缓存，供 JWT 过滤器使用）。
 */
@Service
@RequiredArgsConstructor
public class UserActiveChecker {

    private final SysUserMapper userMapper;
    private final UserStatusCacheService userStatusCacheService;

    /**
     * 判断用户是否启用。
     *
     * @param userId 用户 ID
     * @return true=启用
     */
    public boolean isUserActive(Long userId) {
        if (userId == null) {
            return false;
        }
        return userStatusCacheService.isUserActive(userId, () -> {
            SysUser user = userMapper.selectById(userId);
            return user != null && user.getStatus() != null && user.getStatus() == 1;
        });
    }

    /**
     * 失效用户状态缓存。
     *
     * @param userId 用户 ID
     */
    public void invalidateCache(Long userId) {
        userStatusCacheService.invalidate(userId);
    }
}
