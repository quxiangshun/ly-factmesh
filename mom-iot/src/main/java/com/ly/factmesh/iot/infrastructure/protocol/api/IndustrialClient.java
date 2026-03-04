package com.ly.factmesh.iot.infrastructure.protocol.api;

import java.util.Map;

/**
 * 工业协议客户端通用接口
 * 屏蔽 OPC UA / Modbus TCP 等协议差异，统一对外提供读写能力
 *
 * @author LY-FactMesh
 */
public interface IndustrialClient {

    /**
     * 连接设备
     *
     * @param endpointUrl OPC UA 端点地址，或 Modbus TCP 时为 host:port 格式
     * @param username    用户名（可选，OPC UA 用；Modbus 可忽略）
     * @param password    密码（可选，OPC UA 用；Modbus 可忽略）
     * @param connectTimeoutMs 连接超时毫秒
     * @return 是否连接成功
     */
    boolean connect(String endpointUrl, String username, String password, int connectTimeoutMs);

    /**
     * 读取单个值
     * OPC UA：nodeId 如 ns=2;s=Temperature
     * Modbus：pointId 格式 slaveId_registerType_registerAddr，如 1_3_100（3=保持寄存器）
     *
     * @param pointId 点位标识
     * @return 读取的值
     */
    Object readSingleValue(String pointId);

    /**
     * 批量读取值
     *
     * @param pointIds 点位列表
     * @return 点位 -> 值 映射
     */
    Map<String, Object> readBatchValues(String[] pointIds);

    /**
     * 写入单个值
     *
     * @param pointId 点位标识
     * @param value   写入值
     * @return 是否成功
     */
    boolean writeSingleValue(String pointId, Object value);

    /**
     * 批量写入值
     *
     * @param pointValueMap 点位 -> 值 映射
     * @return 是否成功
     */
    boolean writeBatchValues(Map<String, Object> pointValueMap);

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 是否已连接
     */
    boolean isConnected();
}
