package com.mulehang.blog.exception;

import com.mulehang.blog.exception.BusinessException;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.model.ResultCodeEnum;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理（统一返回 {@link Result}）。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @param e 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常 [{}]: {}", e.getCode().getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理非法参数异常（兼容历史代码）。
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.fail(ResultCodeEnum.BAD_REQUEST, e.getMessage());
    }

    /**
     * 处理非法状态异常（如未登录）。
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalStateException.class)
    public Result<?> handleIllegalStateException(IllegalStateException e) {
        return Result.fail(ResultCodeEnum.UNAUTHORIZED, e.getMessage());
    }

    /**
     * 处理方法参数校验异常。
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors().isEmpty()
                ? "参数校验失败"
                : e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return Result.fail(ResultCodeEnum.BAD_REQUEST, msg);
    }

    /**
     * 处理约束违反异常。
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        return Result.fail(ResultCodeEnum.BAD_REQUEST, e.getMessage());
    }

    /**
     * 处理 Spring Security 认证异常。
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<?> handleAuthenticationException(AuthenticationException e) {
        return Result.fail(ResultCodeEnum.UNAUTHORIZED, "未认证或 Token 无效");
    }

    /**
     * 处理权限不足异常。
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        return Result.fail(ResultCodeEnum.FORBIDDEN, "权限不足");
    }

    /**
     * 处理历史裸 RuntimeException 业务异常。
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        if (e.getClass().equals(RuntimeException.class) && StringUtils.hasText(e.getMessage())) {
            log.warn("业务运行时异常: {}", e.getMessage());
            return Result.fail(ResultCodeEnum.BAD_REQUEST, e.getMessage());
        }
        log.error("未处理运行时异常", e);
        return Result.fail(ResultCodeEnum.INTERNAL_ERROR, "服务器内部错误");
    }

    /**
     * 处理通用异常。
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("未处理异常", e);
        return Result.fail(ResultCodeEnum.INTERNAL_ERROR, "服务器内部错误");
    }
}
