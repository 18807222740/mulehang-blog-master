package com.mulehang.blog.controller;

import com.mulehang.blog.context.UserContext;
import com.mulehang.blog.dto.UserPasswordUpdateDTO;
import com.mulehang.blog.dto.UserUpdateDTO;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.CommentService;
import com.mulehang.blog.service.UserService;
import com.mulehang.blog.vo.CommentVO;
import com.mulehang.blog.vo.UserInfoVO;
import com.mulehang.blog.vo.UserStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息控制器
 */
@Tag(name = "用户管理", description = "用户信息相关接口")
@RestController
@RequestMapping("/api/v1/users")  // 修复：改为 users（复数）与文档一致
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CommentService commentService;

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息（需要认证）")
    @GetMapping("/current")
    public Result<UserInfoVO> getCurrentUser() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        return Result.ok(userService.getUserInfo(userId));
    }

    /**
     * 更新当前用户资料
     *
     * @param dto 用户资料更新 DTO
     * @return 更新后的用户信息
     */
    @Operation(summary = "更新当前用户资料", description = "修改昵称、头像、邮箱、个人简介（需要认证）")
    @PutMapping("/current")
    public Result<UserInfoVO> updateCurrentUser(@Valid @RequestBody UserUpdateDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        return Result.ok(userService.updateProfile(userId, dto));
    }

    /**
     * 修改当前用户密码
     *
     * @param dto 密码修改 DTO
     * @return 操作结果
     */
    @Operation(summary = "修改当前用户密码", description = "校验旧密码后更新为新密码（需要认证）")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody UserPasswordUpdateDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        userService.changePassword(userId, dto);
        return Result.ok();
    }

    /**
     * 删除当前账号
     *
     * @return 操作结果
     */
    @Operation(summary = "删除当前账号", description = "删除当前登录用户（需要认证）")
    @DeleteMapping("/current")
    public Result<Void> deleteCurrentUser() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        userService.deleteAccount(userId);
        return Result.ok();
    }

    /**
     * 获取当前用户统计信息
     *
     * @return 用户统计信息
     */
    @Operation(summary = "获取当前用户统计信息", description = "获取当前登录用户的文章/评论/点赞统计（需要认证）")
    @GetMapping("/current/stats")
    public Result<UserStatsVO> getCurrentUserStats() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        return Result.ok(userService.getUserStats(userId));
    }

    /**
     * 获取当前用户评论列表
     *
     * @param pageNo   页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 评论分页列表
     */
    @Operation(summary = "获取当前用户评论列表", description = "分页查询当前登录用户发表的评论（需要认证）")
    @GetMapping("/current/comments")
    public Result<PageResult<CommentVO>> getCurrentUserComments(Long pageNo, Long pageSize) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        return Result.ok(commentService.listByUser(userId, pageNo, pageSize));
    }
}
