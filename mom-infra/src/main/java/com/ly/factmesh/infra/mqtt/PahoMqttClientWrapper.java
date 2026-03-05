package com.ly.factmesh.infra.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 基于 Eclipse Paho 的 MQTT 客户端实现
 * 支持配置化订阅、自动重连、消息回调分发
 *
 * @author LY-FactMesh
 */
@Component
@ConditionalOnProperty(prefix = "infra.mqtt", name = "broker-url")
@ConditionalOnMissingBean(MqttClientWrapper.class)
public class PahoMqttClientWrapper implements MqttClientWrapper {

    private static final Logger log = LoggerFactory.getLogger(PahoMqttClientWrapper.class);

    @Autowired
    private MqttProperties mqttProperties;

    @Autowired(required = false)
    private MqttMessageHandler defaultMessageHandler;

    private MqttClient mqttClient;
    /** 主题过滤模式 -> 回调列表（支持通配符订阅） */
    private final Map<String, List<MessageCallback>> topicCallbacks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false);
            if (mqttProperties.getUsername() != null) {
                options.setUserName(mqttProperties.getUsername());
            }
            if (mqttProperties.getPassword() != null) {
                options.setPassword(mqttProperties.getPassword().toCharArray());
            }
            options.setConnectionTimeout(mqttProperties.getConnectTimeout());
            options.setKeepAliveInterval(mqttProperties.getKeepAlive());
            options.setAutomaticReconnect(mqttProperties.isAutoReconnect());
            options.setMaxReconnectDelay((int) (mqttProperties.getMaxReconnectInterval() * 1000));

            if (mqttProperties.isEnableTls() && mqttProperties.getTlsCertPath() != null) {
                // TLS 配置需根据实际证书路径实现
                log.warn("MQTT TLS 已启用，tlsCertPath={}，需自行配置 SSLContext", mqttProperties.getTlsCertPath());
            }

            String clientId = mqttProperties.getClientId();
            if (clientId == null || clientId.isBlank()) {
                clientId = MqttClient.generateClientId();
            }

            mqttClient = new MqttClient(
                    mqttProperties.getBrokerUrl(),
                    clientId,
                    new MemoryPersistence()
            );

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("MQTT 连接断开，原因：{}", cause != null ? cause.getMessage() : "unknown", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    byte[] payload = message.getPayload();
                    dispatchMessage(topic, payload);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    if (token != null && token.getException() != null) {
                        String[] topics = token.getTopics();
                        log.error("MQTT 消息发布失败，topic：{}", topics != null && topics.length > 0 ? topics[0] : "unknown", token.getException());
                    }
                }
            });

            mqttClient.connect(options);

            Map<String, Integer> subscribeTopics = mqttProperties.getSubscribeTopics();
            if (subscribeTopics != null && !subscribeTopics.isEmpty()) {
                for (Map.Entry<String, Integer> entry : subscribeTopics.entrySet()) {
                    String topic = entry.getKey();
                    int qos = entry.getValue() != null ? entry.getValue() : 1;
                    mqttClient.subscribe(topic, qos);
                    log.info("MQTT 订阅主题成功：{}，QoS：{}", topic, qos);
                }
            }

            log.info("MQTT 客户端初始化成功，broker：{}", mqttProperties.getBrokerUrl());
        } catch (MqttException e) {
            log.error("MQTT 客户端初始化失败", e);
            throw new RuntimeException("MQTT init failed", e);
        }
    }

    /**
     * 将消息分发给匹配的订阅回调，以及可选的全局处理器
     */
    private void dispatchMessage(String topic, byte[] payload) {
        for (Map.Entry<String, List<MessageCallback>> entry : topicCallbacks.entrySet()) {
            if (topicMatches(entry.getKey(), topic)) {
                for (MessageCallback cb : entry.getValue()) {
                    try {
                        cb.onMessage(topic, payload);
                    } catch (Exception e) {
                        log.error("MQTT 消息回调执行异常，topic：{}", topic, e);
                    }
                }
            }
        }
        if (defaultMessageHandler != null) {
            try {
                defaultMessageHandler.onMessage(topic, payload);
            } catch (Exception e) {
                log.error("MQTT 全局处理器执行异常，topic：{}", topic, e);
            }
        }
    }

    /**
     * MQTT 主题匹配：filter 支持 +（单层）和 #（多层）
     */
    private boolean topicMatches(String filter, String topic) {
        String[] filterParts = filter.split("/", -1);
        String[] topicParts = topic.split("/", -1);
        int fi = 0, ti = 0;
        while (fi < filterParts.length && ti < topicParts.length) {
            String fp = filterParts[fi];
            if ("#".equals(fp)) {
                return true;
            }
            if ("+".equals(fp)) {
                fi++;
                ti++;
                continue;
            }
            if (!fp.equals(topicParts[ti])) {
                return false;
            }
            fi++;
            ti++;
        }
        if (fi < filterParts.length) {
            return filterParts.length == fi + 1 && "#".equals(filterParts[fi]);
        }
        return ti == topicParts.length;
    }

    @Override
    public void publish(String topic, byte[] payload) {
        publish(topic, payload, 1);
    }

    @Override
    public void publish(String topic, byte[] payload, int qos) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            log.warn("MQTT 未连接，无法发布消息，topic：{}", topic);
            return;
        }
        try {
            MqttMessage message = new MqttMessage(payload);
            message.setQos(qos);
            mqttClient.publish(topic, message);
            log.debug("发布 MQTT 消息，topic：{}", topic);
        } catch (MqttException e) {
            log.error("MQTT 消息发布失败，topic：{}", topic, e);
            throw new RuntimeException("MQTT publish failed", e);
        }
    }

    @Override
    public void subscribe(String topic, MessageCallback callback) {
        topicCallbacks.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(callback);
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.subscribe(topic, 1);
                log.info("MQTT 动态订阅：{}", topic);
            } catch (MqttException e) {
                log.error("MQTT 订阅失败，topic：{}", topic, e);
            }
        }
    }

    @Override
    public void unsubscribe(String topic) {
        topicCallbacks.remove(topic);
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.unsubscribe(topic);
                log.info("MQTT 取消订阅：{}", topic);
            } catch (MqttException e) {
                log.error("MQTT 取消订阅失败，topic：{}", topic, e);
            }
        }
    }

    @Override
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    @Override
    public void disconnect() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                log.info("MQTT 客户端已断开");
            } catch (MqttException e) {
                log.error("MQTT 断开失败", e);
            }
        }
    }

    @PreDestroy
    public void destroy() {
        if (mqttClient != null) {
            try {
                if (mqttClient.isConnected()) {
                    mqttClient.disconnect();
                }
                mqttClient.close();
                log.info("MQTT 客户端已关闭");
            } catch (MqttException e) {
                log.error("关闭 MQTT 客户端失败", e);
            }
        }
    }
}
