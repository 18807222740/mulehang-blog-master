package com.mulehang.blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户资料更新 DTO
 */
@Data
public class UserUpdateDTO {

    /**
     * 昵称
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称长度不能超过64位")
    private String nickname;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 头像 URL
     */
    @Size(max = 255, message = "头像地址长度不能超过255位")
    private String avatar;

    /**
     * 个人简介
     */
    @Size(max = 200, message = "个人简介长度不能超过200位")
    private String profile;
}
