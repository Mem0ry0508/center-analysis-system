package org.center.util;

/**
 * 保存「目前登入操作者」的 account_id，供 service 層寫稽核紀錄時取用，
 * 避免 service 反向相依 ui 的 Session。登入時設定、登出時清除。
 */
public final class AuditContext {

    private static volatile Long actorId;

    private AuditContext() {
    }

    public static void setActorId(Long id) {
        actorId = id;
    }

    public static Long currentActorId() {
        return actorId;
    }

    public static void clear() {
        actorId = null;
    }
}
