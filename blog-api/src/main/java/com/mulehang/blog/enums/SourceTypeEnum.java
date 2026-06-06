package com.mulehang.blog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章来源类型枚举。
 */
@Getter
@AllArgsConstructor
public enum SourceTypeEnum {

    ORIGINAL(1, "原创"),
    REPRINT(2, "转载"),
    TRANSLATION(3, "翻译");

    private final int code;
    private final String desc;

    /**
     * 根据 code 获取枚举。
     */
    public static SourceTypeEnum fromCode(int code) {
        for (SourceTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
