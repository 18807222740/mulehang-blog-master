package com.mulehang.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户密码修改 DTO
 */
@Data
public class UserPasswordUpdateDTO {

    /**
     * 当前密码
     */
    @NotBlank(message = "当前密码不能为空")
    private String currentPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度为6-20位")
    private String newPassword;
}
