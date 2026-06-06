package com.mulehang.blog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章状态枚举。
 */
@Getter
@AllArgsConstructor
public enum ArticleStatusEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    PRIVATE(2, "私密");

    private final int code;
    private final String desc;

    /**
     * 根据 code 获取枚举。
     */
    public static ArticleStatusEnum fromCode(int code) {
        for (ArticleStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
