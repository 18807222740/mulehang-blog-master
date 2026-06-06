package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.exception.BusinessException;
import com.mulehang.blog.dto.UserPasswordUpdateDTO;
import com.mulehang.blog.dto.UserUpdateDTO;
import com.mulehang.blog.entity.SysUserRole;
import com.mulehang.blog.entity.SysUser;
import com.mulehang.blog.mapper.BlogArticleMapper;
import com.mulehang.blog.mapper.BlogCommentMapper;
import com.mulehang.blog.mapper.SysUserMapper;
import com.mulehang.blog.mapper.SysUserRoleMapper;
import com.mulehang.blog.service.UserService;
import com.mulehang.blog.util.PasswordUtil;
import com.mulehang.blog.vo.UserInfoVO;
import com.mulehang.blog.vo.UserStatsVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordUtil passwordUtil;
    private final BlogArticleMapper articleMapper;
    private final BlogCommentMapper commentMapper;

    /**
     * 构造函数（构造器注入）。
     *
     * @param userMapper     用户 Mapper
     * @param userRoleMapper 用户角色 Mapper
     * @param passwordUtil   密码工具
     * @param articleMapper  文章 Mapper
     * @param commentMapper  评论 Mapper
     */
    public UserServiceImpl(SysUserMapper userMapper,
                           SysUserRoleMapper userRoleMapper,
                           PasswordUtil passwordUtil,
                           BlogArticleMapper articleMapper,
                           BlogCommentMapper commentMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordUtil = passwordUtil;
        this.articleMapper = articleMapper;
        this.commentMapper = commentMapper;
    }

    /**
     * 获取用户信息。
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    @Override
    public UserInfoVO getUserInfo(Long userId) {
        SysUser user = requireUser(userId);
        return toUserInfo(user);
    }

    /**
     * 更新用户资料。
     *
     * @param userId 用户 ID
     * @param dto    更新 DTO
     * @return 更新后的用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO updateProfile(Long userId, UserUpdateDTO dto) {
        if (dto == null) {
            throw BusinessException.badRequest("参数 dto 不能为空");
        }
        SysUser user = requireUser(userId);
        String nickname = normalizeText(dto.getNickname());
        if (!StringUtils.hasText(nickname)) {
            throw BusinessException.badRequest("昵称不能为空");
        }
        String email = normalizeText(dto.getEmail());
        if (StringUtils.hasText(email) && !email.equals(user.getEmail())) {
            Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, email)
                    .ne(SysUser::getId, userId));
            if (count != null && count > 0) {
                throw BusinessException.badRequest("邮箱已被其他用户使用");
            }
        }
        user.setNickname(nickname);
        user.setEmail(email);
        user.setAvatar(normalizeText(dto.getAvatar()));
        user.setProfile(normalizeText(dto.getProfile()));
        userMapper.updateById(user);
        return toUserInfo(user);
    }

    /**
     * 修改用户密码。
     *
     * @param userId 用户 ID
     * @param dto    密码修改 DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, UserPasswordUpdateDTO dto) {
        if (dto == null) {
            throw BusinessException.badRequest("参数 dto 不能为空");
        }
        SysUser user = requireUser(userId);
        if (!passwordUtil.matches(dto.getCurrentPassword(), user.getPasswordSalt(), user.getPasswordHash())) {
            throw BusinessException.badRequest("当前密码不正确");
        }
        String salt = passwordUtil.generateSalt();
        String hash = passwordUtil.encryptPassword(dto.getNewPassword(), salt);
        user.setPasswordSalt(salt);
        user.setPasswordHash(hash);
        userMapper.updateById(user);
    }

    /**
     * 删除当前账号。
     *
     * @param userId 用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccount(Long userId) {
        SysUser user = requireUser(userId);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        userMapper.deleteById(user.getId());
    }

    /**
     * 获取用户统计信息。
     *
     * @param userId 用户 ID
     * @return 用户统计
     */
    @Override
    public UserStatsVO getUserStats(Long userId) {
        requireUser(userId);
        Long articleCount = articleMapper.countPublishedByAuthor(userId);
        Long commentCount = commentMapper.countApprovedByUser(userId);
        Long likeCount = articleMapper.sumLikeCountByAuthor(userId);

        UserStatsVO stats = new UserStatsVO();
        stats.setArticleCount(articleCount == null ? 0L : articleCount);
        stats.setCommentCount(commentCount == null ? 0L : commentCount);
        stats.setLikeCount(likeCount == null ? 0L : likeCount);
        return stats;
    }

    /**
     * 获取用户列表（管理员）。
     *
     * @return 用户信息列表
     */
    @Override
    public List<UserInfoVO> listUsers() {
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .orderByDesc(SysUser::getId));
        return users.stream()
                .map(this::toUserInfo)
                .toList();
    }

    /**
     * 规范化文本字段（trim + 空值处理）。
     *
     * @param value 原始值
     * @return 规范化后的值
     */
    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 校验并获取用户。
     *
     * @param userId 用户 ID
     * @return 用户实体
     */
    private SysUser requireUser(Long userId) {
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return user;
    }

    /**
     * 转换为用户信息 VO。
     *
     * @param user 用户实体
     * @return 用户信息
     */
    private UserInfoVO toUserInfo(SysUser user) {
        List<String> roles = userRoleMapper.selectRoleCodesByUserId(user.getId());
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .profile(user.getProfile())
                .roles(roles)
                .lastLoginTime(user.getLastLoginTime())
                .build();
    }
}
