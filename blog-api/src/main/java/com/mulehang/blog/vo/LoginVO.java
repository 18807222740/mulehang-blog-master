package com.mulehang.blog.vo;

import lombok.Data;

/**
 * 用户登录 VO
 */
@Data
public class LoginVO {
    private String accessToken;// 访问令牌

    private String tokenType;// 令牌类型

    private Long expiresIn;// 过期时间

    private UserVO user;// 用户信息
}
