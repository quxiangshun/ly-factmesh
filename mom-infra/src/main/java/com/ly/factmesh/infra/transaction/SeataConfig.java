package com.ly.factmesh.infra.transaction;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Seata 分布式事务配置占位
 * 当 seata 相关依赖在 classpath 且 seata.enabled=true 时生效
 * 使用方式：业务模块添加 io.seata:seata-spring-boot-starter 依赖
 *
 * @author LY-FactMesh
 */
@Configuration
@ConditionalOnProperty(prefix = "seata", name = "enabled", havingValue = "true")
public class SeataConfig {
    // Seata 自动配置由 seata-spring-boot-starter 提供
    // 此处仅作说明，实际配置见各业务模块 application.yml
}
