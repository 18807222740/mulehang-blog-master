package com.mulehang.blog.context;

import com.mulehang.blog.vo.UserInfoVO;

/**
 * 用户上下文（ThreadLocal）
 * 用于在请求处理过程中传递当前登录用户信息
 */
public class UserContext {

    private static final ThreadLocal<UserInfoVO> USER_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户信息
     *
     * @param userInfo 用户信息
     */
    public static void setCurrentUser(UserInfoVO userInfo) {
        USER_HOLDER.set(userInfo);
    }

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    public static UserInfoVO getCurrentUser() {
        return USER_HOLDER.get();
    }

    /**
     * 获取当前用户 ID
     *
     * @return 用户 ID
     */
    public static Long getCurrentUserId() {
        UserInfoVO user = USER_HOLDER.get();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public static String getCurrentUsername() {
        UserInfoVO user = USER_HOLDER.get();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 清除当前用户信息
     */
    public static void clear() {
        USER_HOLDER.remove();
    }
}

