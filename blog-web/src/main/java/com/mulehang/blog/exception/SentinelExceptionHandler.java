package com.mulehang.blog.exception;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.model.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sentinel 全局异常处理器
 * 统一处理 Sentinel 限流、熔断、降级异常
 */
@Slf4j
@RestControllerAdvice
@Order(0)  // 优先级最高，优先捕获 Sentinel 异常
public class SentinelExceptionHandler {

    /**
     * 处理流控异常
     */
    @ExceptionHandler(FlowException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<?> handleFlowException(FlowException e) {
        log.warn("触发流控规则: resource={}, limitApp={}", 
                e.getRule().getResource(), 
                e.getRule().getLimitApp());
        return Result.fail("请求过于频繁，请稍后再试");
    }

    /**
     * 处理熔断降级异常
     */
    @ExceptionHandler(DegradeException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result<?> handleDegradeException(DegradeException e) {
        log.warn("触发熔断降级: resource={}, grade={}", 
                e.getRule().getResource(), 
                e.getRule().getGrade());
        return Result.fail("服务暂时不可用，请稍后再试");
    }

    /**
     * 处理授权规则异常
     */
    @ExceptionHandler(AuthorityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAuthorityException(AuthorityException e) {
        log.warn("触发授权规则: resource={}", e.getRule().getResource());
        return Result.fail(ResultCodeEnum.FORBIDDEN, "访问被拒绝");
    }

    /**
     * 处理所有 Sentinel BlockException（兜底）
     */
    @ExceptionHandler(BlockException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<?> handleBlockException(BlockException e) {
        log.warn("触发 Sentinel 保护规则: ruleLimitApp={}", e.getRuleLimitApp());
        return Result.fail("系统繁忙，请稍后再试");
    }
}
