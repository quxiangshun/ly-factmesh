package com.ly.factmesh.common.utils;

/**
 * 校验工具类：字符串判空、非空断言、条件断言
 *
 * @author LY-FactMesh
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    /** 是否为空或仅空白字符 */
    public static boolean isBlank(CharSequence cs) {
        return cs == null || cs.toString().trim().isEmpty();
    }

    /** 是否非空且非仅空白 */
    public static boolean notBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /** 要求非空，否则抛 IllegalArgumentException */
    public static void requireNotBlank(CharSequence cs, String message) {
        if (isBlank(cs)) {
            throw new IllegalArgumentException(message != null ? message : "参数不能为空");
        }
    }

    /** 要求非 null，否则抛 IllegalArgumentException */
    public static void requireNonNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message != null ? message : "参数不能为空");
        }
    }

    /** 要求条件为 true，否则抛 IllegalArgumentException */
    public static void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message != null ? message : "校验失败");
        }
    }
}
