package com.mulehang.blog.vo;

import lombok.Data;

/**
 * 用户 VO
 */
@Data
public class UserVO {

    private Long id;// 用户ID

    private String username;// 用户名

    private String nickname;// 昵称

    private String avatar;// 头像

    private String profile;// 个人简介
}
