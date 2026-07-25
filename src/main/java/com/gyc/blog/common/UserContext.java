package com.gyc.blog.common;

public class UserContext {
    // 使用 ThreadLocal 存储当前线程的用户ID（线程安全）
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    // 请求结束后清除，防止内存泄漏
    public static void remove() {
        USER_ID.remove();
    }
}