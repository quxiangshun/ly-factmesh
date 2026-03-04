package com.ly.factmesh.iot.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * REST 客户端配置（模拟器对接等场景使用）
 *
 * @author LY-FactMesh
 */
@Configuration
public class RestClientConfig {

    @Bean
    @ConditionalOnProperty(name = "iot.simulator-collect.enabled", havingValue = "true")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
