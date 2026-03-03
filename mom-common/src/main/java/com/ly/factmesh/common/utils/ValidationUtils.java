package com.ly.factmesh.common.utils;

/**
 * 校验工具类
 *
 * @author LY-FactMesh
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static boolean isBlank(CharSequence cs) {
        return cs == null || cs.toString().trim().isEmpty();
    }

    public static boolean notBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    public static void requireNotBlank(CharSequence cs, String message) {
        if (isBlank(cs)) {
            throw new IllegalArgumentException(message != null ? message : "参数不能为空");
        }
    }

    public static void requireNonNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message != null ? message : "参数不能为空");
        }
    }

    public static void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message != null ? message : "校验失败");
        }
    }
}
