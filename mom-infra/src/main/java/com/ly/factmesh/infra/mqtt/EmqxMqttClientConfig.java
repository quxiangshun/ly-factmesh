package com.ly.factmesh.infra.mqtt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * EMQX/MQTT 客户端配置
 * 当 infra.mqtt.broker-url 已配置时生效，自动注册 PahoMqttClientWrapper 实现
 *
 * @author LY-FactMesh
 */
@Configuration
@EnableConfigurationProperties(MqttProperties.class)
@ConditionalOnProperty(prefix = "infra.mqtt", name = "broker-url")
public class EmqxMqttClientConfig {
}
