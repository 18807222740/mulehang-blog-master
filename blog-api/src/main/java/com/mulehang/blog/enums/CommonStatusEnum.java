package com.mulehang.blog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举（启用/禁用）。
 * 适用于：用户、角色、分类、专栏等。
 */
@Getter
@AllArgsConstructor
public enum CommonStatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final int code;
    private final String desc;

    /**
     * 根据 code 获取枚举。
     */
    public static CommonStatusEnum fromCode(int code) {
        for (CommonStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
