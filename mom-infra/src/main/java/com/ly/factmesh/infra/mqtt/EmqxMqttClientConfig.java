package com.ly.factmesh.infra.mqtt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EMQX/MQTT 客户端配置
 * 当 infra.mqtt.broker-url 已配置且 Paho 客户端在 classpath 时生效
 * 使用方式：在业务模块添加 org.eclipse.paho:org.eclipse.paho.client.mqttv3 依赖
 *
 * @author LY-FactMesh
 */
@Configuration
@EnableConfigurationProperties(MqttProperties.class)
@ConditionalOnProperty(prefix = "infra.mqtt", name = "broker-url")
public class EmqxMqttClientConfig {
    // MQTT 客户端 Bean 由 mom-iot 等业务模块按需实现并注入
    // 此处仅提供配置绑定，具体 MqttClientWrapper 实现可放在 mom-iot
}
