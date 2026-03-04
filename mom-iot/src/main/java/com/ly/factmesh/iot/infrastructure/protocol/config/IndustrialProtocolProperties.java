package com.ly.factmesh.iot.infrastructure.protocol.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iot.industrial")
@Data
public class IndustrialProtocolProperties {

    private boolean enabled = false;
    private OpcUaConfig opcua = new OpcUaConfig();
    private ModbusConfig modbus = new ModbusConfig();

    @Data
    public static class OpcUaConfig {
        private String endpointUrl = "opc.tcp://127.0.0.1:4840";
        private String username;
        private String password;
        private int connectTimeoutMs = 5000;
        private int poolMaxTotal = 10;
        private int poolMaxIdle = 5;
        private int poolMinIdle = 2;
    }

    @Data
    public static class ModbusConfig {
        private String host = "127.0.0.1";
        private int port = 502;
        private int connectTimeoutMs = 5000;
        private int retryCount = 3;
        private int poolMaxTotal = 10;
        private int poolMaxIdle = 5;
        private int poolMinIdle = 2;
    }
}
