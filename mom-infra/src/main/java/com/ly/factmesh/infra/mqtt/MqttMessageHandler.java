package com.ly.factmesh.infra.mqtt;

/**
 * MQTT 消息全局处理器（可选）
 * 实现此接口并注册为 Bean，可接收所有 MQTT 消息（含配置订阅主题）
 * 用于 FactMesh 事实数据接入、转发等场景
 *
 * @author LY-FactMesh
 */
@FunctionalInterface
public interface MqttMessageHandler {

    /**
     * 收到 MQTT 消息时回调
     *
     * @param topic   主题
     * @param payload 载荷
     */
    void onMessage(String topic, byte[] payload);
}
