package com.mulehang.blog.service;

import com.mulehang.blog.dto.UserPasswordUpdateDTO;
import com.mulehang.blog.dto.UserUpdateDTO;
import com.mulehang.blog.vo.UserInfoVO;
import com.mulehang.blog.vo.UserStatsVO;

import java.util.List;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 获取用户信息。
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 更新用户资料。
     *
     * @param userId 用户 ID
     * @param dto    更新 DTO
     * @return 更新后的用户信息
     */
    UserInfoVO updateProfile(Long userId, UserUpdateDTO dto);

    /**
     * 修改用户密码。
     *
     * @param userId 用户 ID
     * @param dto    密码修改 DTO
     */
    void changePassword(Long userId, UserPasswordUpdateDTO dto);

    /**
     * 删除当前账号。
     *
     * @param userId 用户 ID
     */
    void deleteAccount(Long userId);

    /**
     * 获取用户统计信息。
     *
     * @param userId 用户 ID
     * @return 用户统计
     */
    UserStatsVO getUserStats(Long userId);

    /**
     * 获取用户列表（管理员）。
     *
     * @return 用户信息列表
     */
    List<UserInfoVO> listUsers();
}
