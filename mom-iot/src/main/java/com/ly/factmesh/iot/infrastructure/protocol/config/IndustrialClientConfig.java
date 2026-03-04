package com.ly.factmesh.iot.infrastructure.protocol.config;

import lombok.Data;

/**
 * 工业协议连接配置（工厂/池化使用）
 *
 * @author LY-FactMesh
 */
@Data
public class IndustrialClientConfig {

    /**
     * OPC UA 端点地址，如 opc.tcp://192.168.1.100:4840
     * Modbus TCP 时可为空，使用 host:port
     */
    private String endpointUrl;

    private String host = "127.0.0.1";
    private int port = 502;
    private String username;
    private String password;
    private int connectTimeoutMs = 5000;
    private int retryCount = 3;
}
