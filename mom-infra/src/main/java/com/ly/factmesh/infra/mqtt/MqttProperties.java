package com.ly.factmesh.infra.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * MQTT 连接配置（EMQX 等）
 * 支持连接、重连、订阅主题、TLS 等完整配置
 *
 * @author LY-FactMesh
 */
@ConfigurationProperties(prefix = "infra.mqtt")
public class MqttProperties {

    /** Broker 地址，如 tcp://127.0.0.1:1883 */
    private String brokerUrl = "tcp://localhost:1883";
    private String clientId;
    private String username;
    private String password;
    /** 连接超时（秒） */
    private int connectTimeout = 5;
    /** 保活间隔（秒） */
    private int keepAlive = 30;
    private boolean autoReconnect = true;
    /** 最大重连间隔（秒），指数退避上限 */
    private long maxReconnectInterval = 10;
    /** 订阅主题：key=主题（支持通配符 +/#），value=QoS */
    private Map<String, Integer> subscribeTopics;
    /** 是否启用 TLS */
    private boolean enableTls = false;
    /** TLS 证书路径（生产环境配置） */
    private String tlsCertPath;

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public long getMaxReconnectInterval() {
        return maxReconnectInterval;
    }

    public void setMaxReconnectInterval(long maxReconnectInterval) {
        this.maxReconnectInterval = maxReconnectInterval;
    }

    public Map<String, Integer> getSubscribeTopics() {
        return subscribeTopics;
    }

    public void setSubscribeTopics(Map<String, Integer> subscribeTopics) {
        this.subscribeTopics = subscribeTopics;
    }

    public boolean isEnableTls() {
        return enableTls;
    }

    public void setEnableTls(boolean enableTls) {
        this.enableTls = enableTls;
    }

    public String getTlsCertPath() {
        return tlsCertPath;
    }

    public void setTlsCertPath(String tlsCertPath) {
        this.tlsCertPath = tlsCertPath;
    }
}
