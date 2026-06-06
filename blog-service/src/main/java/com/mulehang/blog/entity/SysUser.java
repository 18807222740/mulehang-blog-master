package com.mulehang.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;// 主键

    private String username;// 用户名

    private String passwordHash;// 密码哈希

    private String passwordSalt;// 密码盐

    private String nickname;// 昵称

    private String email;// 邮箱

    private String mobile;// 手机号

    private String avatar;// 头像

    private String profile;// 个人简介

    private Integer status;// 状态

    private String registerIp;// 注册IP

    private String lastLoginIp;// 最后登录IP

    private LocalDateTime lastLoginTime;// 最后登录时间
}
