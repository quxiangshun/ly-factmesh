package com.ly.factmesh.iot.infrastructure.protocol.factory;

import com.ly.factmesh.iot.infrastructure.protocol.api.IndustrialClient;
import com.ly.factmesh.iot.infrastructure.protocol.adapter.ModbusTcpClientAdapter;
import com.ly.factmesh.iot.infrastructure.protocol.adapter.OpcUaClientAdapter;
import com.ly.factmesh.iot.infrastructure.protocol.config.IndustrialClientConfig;
import com.ly.factmesh.iot.infrastructure.protocol.exception.IndustrialConnException;

/**
 * 工业客户端工厂，根据协议类型创建对应适配器
 *
 * @author LY-FactMesh
 */
public final class IndustrialClientFactory {

    public static final String OPC_UA = "OPC_UA";
    public static final String MODBUS_TCP = "MODBUS_TCP";

    private IndustrialClientFactory() {
    }

    /**
     * 创建指定协议的客户端并连接
     *
     * @param protocolType 协议类型：OPC_UA / MODBUS_TCP
     * @param config       连接配置
     * @return 已连接的工业客户端
     */
    public static IndustrialClient createClient(String protocolType, IndustrialClientConfig config) {
        IndustrialClient client = createClient(protocolType);
        String endpointUrl;
        if (OPC_UA.equalsIgnoreCase(protocolType)) {
            endpointUrl = config.getEndpointUrl() != null ? config.getEndpointUrl() : "opc.tcp://" + config.getHost() + ":" + config.getPort();
        } else {
            endpointUrl = config.getHost() + ":" + config.getPort();
        }
        client.connect(
                endpointUrl,
                config.getUsername(),
                config.getPassword(),
                config.getConnectTimeoutMs() > 0 ? config.getConnectTimeoutMs() : 5000
        );
        return client;
    }

    /**
     * 创建指定协议的客户端（未连接）
     *
     * @param protocolType 协议类型：OPC_UA / MODBUS_TCP
     * @return 工业客户端
     */
    public static IndustrialClient createClient(String protocolType) {
        switch (protocolType.toUpperCase()) {
            case OPC_UA:
                return new OpcUaClientAdapter();
            case MODBUS_TCP:
                return new ModbusTcpClientAdapter();
            default:
                throw new IndustrialConnException("不支持的协议类型: " + protocolType);
        }
    }
}
