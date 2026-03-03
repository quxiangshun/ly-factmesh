package com.ly.factmesh.infra.mqtt;

/**
 * MQTT 客户端封装接口
 * 对接 EMQX 等 MQTT Broker，用于设备遥测上报、领域事件发布
 *
 * @author LY-FactMesh
 */
public interface MqttClientWrapper {

    /**
     * 发布消息
     *
     * @param topic   主题
     * @param payload 载荷（JSON 或二进制）
     */
    void publish(String topic, byte[] payload);

    /**
     * 发布消息（QoS 可指定）
     *
     * @param topic   主题
     * @param payload 载荷
     * @param qos     0/1/2
     */
    void publish(String topic, byte[] payload, int qos);

    /**
     * 订阅主题
     *
     * @param topic    主题（支持通配符 +/#）
     * @param callback 消息回调
     */
    void subscribe(String topic, MessageCallback callback);

    /**
     * 取消订阅
     *
     * @param topic 主题
     */
    void unsubscribe(String topic);

    /**
     * 是否已连接
     */
    boolean isConnected();

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 消息回调
     */
    @FunctionalInterface
    interface MessageCallback {
        void onMessage(String topic, byte[] payload);
    }
}
