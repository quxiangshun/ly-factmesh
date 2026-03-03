package com.ly.factmesh.infra.database;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 读库切面：@ReadOnly 方法执行前切换至从库
 *
 * @author LY-FactMesh
 */
@Aspect
@Component
@Order(1)
public class ReadOnlyAspect {

    @Around("@annotation(com.ly.factmesh.infra.database.ReadOnly) || @within(com.ly.factmesh.infra.database.ReadOnly)")
    public Object aroundReadOnly(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        boolean needSlave = method.isAnnotationPresent(ReadOnly.class)
                || pjp.getTarget().getClass().isAnnotationPresent(ReadOnly.class);
        if (needSlave) {
            DataSourceContextHolder.setSlave();
        }
        try {
            return pjp.proceed();
        } finally {
            if (needSlave) {
                DataSourceContextHolder.clear();
            }
        }
    }
}
