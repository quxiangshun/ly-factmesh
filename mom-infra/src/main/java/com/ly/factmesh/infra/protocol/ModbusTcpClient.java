package com.ly.factmesh.infra.protocol;

/**
 * Modbus TCP 客户端接口
 * 工业设备数据采集，业务域通过此接口读写寄存器
 *
 * @author LY-FactMesh
 */
public interface ModbusTcpClient {

    /**
     * 连接 Modbus TCP 从站
     *
     * @param host 从站 IP
     * @param port 端口，默认 502
     */
    void connect(String host, int port);

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 是否已连接
     */
    boolean isConnected();

    /**
     * 读保持寄存器
     *
     * @param slaveId    从站 ID
     * @param startAddr  起始地址
     * @param quantity   数量
     * @return 寄存器值数组
     */
    int[] readHoldingRegisters(int slaveId, int startAddr, int quantity);

    /**
     * 读输入寄存器
     *
     * @param slaveId    从站 ID
     * @param startAddr  起始地址
     * @param quantity   数量
     * @return 寄存器值数组
     */
    int[] readInputRegisters(int slaveId, int startAddr, int quantity);

    /**
     * 写单个保持寄存器
     *
     * @param slaveId  从站 ID
     * @param address  地址
     * @param value    值
     */
    void writeSingleRegister(int slaveId, int address, int value);

    /**
     * 写多个保持寄存器
     *
     * @param slaveId  从站 ID
     * @param address  起始地址
     * @param values   值数组
     */
    void writeMultipleRegisters(int slaveId, int address, int[] values);
}
