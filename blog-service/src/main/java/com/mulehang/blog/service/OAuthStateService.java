package com.mulehang.blog.service;

/**
 * OAuth state 管理服务。
 */
public interface OAuthStateService {

    /**
     * 保存 state（并设置过期）。
     *
     * @param state state 值
     */
    void storeState(String state);

    /**
     * 校验并消费 state（一次性）。
     *
     * @param state state 值
     * @return true-校验通过，false-无效或已过期
     */
    boolean consumeState(String state);
}
