package com.mulehang.blog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评论状态枚举。
 */
@Getter
@AllArgsConstructor
public enum CommentStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "通过"),
    REJECTED(2, "拒绝/屏蔽"),
    DELETED(3, "已删除");

    private final int code;
    private final String desc;

    /**
     * 根据 code 获取枚举。
     */
    public static CommentStatusEnum fromCode(int code) {
        for (CommentStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
