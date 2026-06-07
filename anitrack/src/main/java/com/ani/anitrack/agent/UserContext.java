package com.ani.anitrack.agent;

public final class UserContext {
    private static final ThreadLocal<Integer> currentUserId = new InheritableThreadLocal<>();
    private static volatile Integer fallbackUserId;

    private UserContext() {}

    public static void set(Integer userId) {
        currentUserId.set(userId);
        fallbackUserId = userId;
    }

    public static Integer get() {
        Integer id = currentUserId.get();
        return id != null ? id : fallbackUserId;
    }

    public static void clear() {
        currentUserId.remove();
    }
}
