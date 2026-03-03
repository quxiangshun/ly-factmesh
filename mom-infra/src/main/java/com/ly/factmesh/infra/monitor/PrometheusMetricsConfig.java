package com.ly.factmesh.infra.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Prometheus/Grafana 监控埋点配置
 * 当 actuator 与 micrometer 在 classpath 时生效
 *
 * @author LY-FactMesh
 */
@Configuration
@ConditionalOnClass(MeterRegistry.class)
public class PrometheusMetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
                "application", "ly-factmesh"
        );
    }
}
