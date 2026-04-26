package com.wenzi.utils;

/**
 * 用户上下文工具类 (基于 ThreadLocal)
 */
public class UserContext {

    // 创建一个存放 Long 类型 (userId) 的 ThreadLocal
    private static final ThreadLocal<Long> USER_THREAD_LOCAL = new ThreadLocal<>();

    // 存入 userId
    public static void setUserId(Long userId) {
        USER_THREAD_LOCAL.set(userId);
    }

    // 取出 userId
    public static Long getUserId() {
        return USER_THREAD_LOCAL.get();
    }

    // 清除 userId (非常重要！防止内存泄漏和数据串台)
    public static void removeUserId() {
        USER_THREAD_LOCAL.remove();
    }
}