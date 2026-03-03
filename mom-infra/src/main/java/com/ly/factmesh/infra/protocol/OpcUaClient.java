package com.ly.factmesh.infra.protocol;

import java.util.Map;

/**
 * OPC UA 客户端接口
 * 工业设备数据采集，业务域通过此接口读取设备节点数据
 *
 * @author LY-FactMesh
 */
public interface OpcUaClient {

    /**
     * 连接 OPC UA 服务器
     *
     * @param endpointUrl 端点地址，如 opc.tcp://192.168.1.100:4840
     */
    void connect(String endpointUrl);

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 是否已连接
     */
    boolean isConnected();

    /**
     * 读取单个节点值
     *
     * @param nodeId 节点 ID，如 ns=2;s=Device1.Temperature
     * @return 值
     */
    Object readValue(String nodeId);

    /**
     * 批量读取节点值
     *
     * @param nodeIds 节点 ID 列表
     * @return nodeId -> value
     */
    Map<String, Object> readValues(String... nodeIds);

    /**
     * 写入节点值
     *
     * @param nodeId 节点 ID
     * @param value  值
     */
    void writeValue(String nodeId, Object value);
}
