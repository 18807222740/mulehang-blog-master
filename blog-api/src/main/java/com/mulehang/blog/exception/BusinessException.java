package com.mulehang.blog.exception;

import com.mulehang.blog.model.ResultCodeEnum;
import lombok.Getter;

/**
 * 业务异常（携带统一错误码，供全局异常处理器映射）。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ResultCodeEnum code;

    /**
     * 构造业务异常。
     *
     * @param code    错误码枚举
     * @param message 错误信息
     */
    public BusinessException(ResultCodeEnum code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 参数错误或通用客户端错误。
     *
     * @param message 错误信息
     * @return 业务异常
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(ResultCodeEnum.BAD_REQUEST, message);
    }

    /**
     * 未认证。
     *
     * @param message 错误信息
     * @return 业务异常
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(ResultCodeEnum.UNAUTHORIZED, message);
    }

    /**
     * 无权限。
     *
     * @param message 错误信息
     * @return 业务异常
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(ResultCodeEnum.FORBIDDEN, message);
    }

    /**
     * 资源不存在。
     *
     * @param message 错误信息
     * @return 业务异常
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(ResultCodeEnum.NOT_FOUND, message);
    }

    /**
     * 服务端内部错误。
     *
     * @param message 错误信息
     * @return 业务异常
     */
    public static BusinessException internalError(String message) {
        return new BusinessException(ResultCodeEnum.INTERNAL_ERROR, message);
    }
}
