package com.ly.factmesh.common.annotation;

import java.lang.annotation.*;

/**
 * 免认证注解（标记无需 JWT 校验的接口）
 *
 * @author LY-FactMesh
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoAuth {
}
