package com.ly.factmesh.infra.database;

/**
 * 数据源上下文（ThreadLocal 存储当前数据源 key）
 *
 * @author LY-FactMesh
 */
public final class DataSourceContextHolder {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static final String MASTER = "master";
    public static final String SLAVE = "slave";

    private DataSourceContextHolder() {
    }

    public static void setMaster() {
        CONTEXT.set(MASTER);
    }

    public static void setSlave() {
        CONTEXT.set(SLAVE);
    }

    public static String get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
