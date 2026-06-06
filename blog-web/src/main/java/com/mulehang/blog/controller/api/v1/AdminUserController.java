package com.mulehang.blog.controller.api.v1;

import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.UserService;
import com.mulehang.blog.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员用户接口。
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "管理员用户管理", description = "管理员查看用户列表")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * 获取用户列表。
     *
     * @return 用户信息列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取用户列表", description = "管理员查看所有用户基础信息")
    public Result<List<UserInfoVO>> listUsers() {
        return Result.ok(userService.listUsers());
    }
}
