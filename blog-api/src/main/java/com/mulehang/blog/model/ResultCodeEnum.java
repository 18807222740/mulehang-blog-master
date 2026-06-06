package com.mulehang.blog.model;

import lombok.Getter;

/**
 * 统一返回码定义（建议前后端约定后长期稳定）。
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(0, "ok"),

    // 4xx：客户端/参数/权限相关
    BAD_REQUEST(40000, "bad request"),
    UNAUTHORIZED(40100, "unauthorized"),
    FORBIDDEN(40300, "forbidden"),
    NOT_FOUND(40400, "not found"),

    // 5xx：服务端错误
    INTERNAL_ERROR(50000, "internal server error");

    private final int code;
    private final String msg;

    ResultCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
