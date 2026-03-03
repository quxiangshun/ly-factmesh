package com.ly.factmesh.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解（标记需记录日志的接口）
 *
 * @author LY-FactMesh
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    String module() default "";

    String operation() default "";
}
