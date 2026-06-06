package com.mulehang.blog.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 统一响应包装。
 *
 * @param <T> data 类型
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok() {
        Result<T> r = new Result<>();
        r.code = ResultCodeEnum.SUCCESS.getCode();
        r.msg = ResultCodeEnum.SUCCESS.getMsg();
        return r;
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = ok();
        r.data = data;
        return r;
    }

    public static <T> Result<T> fail(String msg) {
        Result<T> r = new Result<>();
        r.code = ResultCodeEnum.BAD_REQUEST.getCode();
        r.msg = msg;
        return r;
    }

    public static <T> Result<T> fail(ResultCodeEnum codeEnum) {
        Result<T> r = new Result<>();
        r.code = codeEnum.getCode();
        r.msg = codeEnum.getMsg();
        return r;
    }

    public static <T> Result<T> fail(ResultCodeEnum codeEnum, String msg) {
        Result<T> r = new Result<>();
        r.code = codeEnum.getCode();
        r.msg = msg;
        return r;
    }
}
