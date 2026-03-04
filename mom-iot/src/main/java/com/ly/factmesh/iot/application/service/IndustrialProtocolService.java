package com.ly.factmesh.iot.application.service;

import com.ly.factmesh.iot.infrastructure.protocol.api.IndustrialClient;
import com.ly.factmesh.iot.infrastructure.protocol.factory.IndustrialClientFactory;
import com.ly.factmesh.iot.infrastructure.protocol.pool.IndustrialClientConnectionPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 工业协议服务（OPC UA、Modbus TCP）示例
 * 当 iot.industrial.enabled=true 且有连接池 Bean 时可用
 *
 * @author LY-FactMesh
 */
@Service
@ConditionalOnBean(IndustrialClientConnectionPool.class)
public class IndustrialProtocolService {

    @Autowired(required = false)
    @Qualifier("opcUaConnectionPool")
    private IndustrialClientConnectionPool opcUaPool;

    @Autowired(required = false)
    @Qualifier("modbusConnectionPool")
    private IndustrialClientConnectionPool modbusPool;

    /**
     * 读取 OPC UA 点位值
     */
    public Object readOpcUaValue(String nodeId) {
        if (opcUaPool == null || !IndustrialClientFactory.OPC_UA.equals(opcUaPool.getProtocolType())) {
            throw new IllegalStateException("OPC UA 连接池未配置");
        }
        IndustrialClient client = null;
        try {
            client = opcUaPool.borrowClient();
            return client.readSingleValue(nodeId);
        } finally {
            if (client != null) {
                opcUaPool.returnClient(client);
            }
        }
    }

    /**
     * 读取 Modbus 点位值，pointId 格式：slaveId_registerType_registerAddr，如 1_3_100
     */
    public Object readModbusValue(String pointId) {
        if (modbusPool == null || !IndustrialClientFactory.MODBUS_TCP.equals(modbusPool.getProtocolType())) {
            throw new IllegalStateException("Modbus TCP 连接池未配置");
        }
        IndustrialClient client = null;
        try {
            client = modbusPool.borrowClient();
            return client.readSingleValue(pointId);
        } finally {
            if (client != null) {
                modbusPool.returnClient(client);
            }
        }
    }

    /**
     * 批量写入 Modbus 寄存器
     */
    public boolean writeModbusBatch(Map<String, Object> pointValueMap) {
        if (modbusPool == null || !IndustrialClientFactory.MODBUS_TCP.equals(modbusPool.getProtocolType())) {
            throw new IllegalStateException("Modbus TCP 连接池未配置");
        }
        IndustrialClient client = null;
        try {
            client = modbusPool.borrowClient();
            return client.writeBatchValues(pointValueMap);
        } finally {
            if (client != null) {
                modbusPool.returnClient(client);
            }
        }
    }
}
