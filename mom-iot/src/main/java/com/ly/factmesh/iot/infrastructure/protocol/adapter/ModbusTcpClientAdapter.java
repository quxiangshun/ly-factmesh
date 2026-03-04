package com.ly.factmesh.iot.infrastructure.protocol.adapter;

import com.ly.factmesh.iot.infrastructure.protocol.api.IndustrialClient;
import com.ly.factmesh.iot.infrastructure.protocol.exception.IndustrialConnException;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.locator.BaseLocator;

import java.util.HashMap;
import java.util.Map;

/**
 * Modbus TCP 协议适配器，实现统一的 IndustrialClient 接口
 * 点位格式：slaveId_registerType_registerAddr，如 1_3_100（3=保持寄存器，4=输入寄存器）
 *
 * @author LY-FactMesh
 */
public class ModbusTcpClientAdapter implements IndustrialClient {

    private ModbusMaster modbusMaster;
    private String host;
    private int port;

    @Override
    public boolean connect(String endpointUrl, String username, String password, int connectTimeoutMs) {
        try {
            if (endpointUrl != null && endpointUrl.contains(":")) {
                String[] parts = endpointUrl.split(":");
                host = parts[0].trim();
                port = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 502;
            } else {
                host = "127.0.0.1";
                port = 502;
            }
            IpParameters params = new IpParameters();
            params.setHost(host);
            params.setPort(port);

            ModbusFactory factory = new ModbusFactory();
            modbusMaster = factory.createTcpMaster(params, true);
            modbusMaster.setTimeout(connectTimeoutMs);
            modbusMaster.setRetries(3);
            modbusMaster.init();
            return true;
        } catch (ModbusInitException e) {
            throw new IndustrialConnException("Modbus TCP 连接失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Object readSingleValue(String pointId) {
        ensureConnected();
        try {
            BaseLocator<?> locator = parsePointId(pointId);
            return modbusMaster.getValue(locator);
        } catch (ModbusTransportException | ErrorResponseException e) {
            throw new IndustrialConnException("Modbus 读取寄存器失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> readBatchValues(String[] pointIds) {
        Map<String, Object> result = new HashMap<>();
        for (String pointId : pointIds) {
            result.put(pointId, readSingleValue(pointId));
        }
        return result;
    }

    @Override
    public boolean writeSingleValue(String pointId, Object value) {
        ensureConnected();
        try {
            String[] parts = pointId.split("_");
            int slaveId = Integer.parseInt(parts[0]);
            int registerType = Integer.parseInt(parts[1]);
            int registerAddr = Integer.parseInt(parts[2]);

            if (registerType == 3) {
                BaseLocator<Number> locator = BaseLocator.holdingRegister(slaveId, registerAddr, DataType.TWO_BYTE_INT_UNSIGNED);
                modbusMaster.setValue(locator, ((Number) value).intValue());
            } else if (registerType == 4) {
                BaseLocator<Number> locator = BaseLocator.inputRegister(slaveId, registerAddr, DataType.TWO_BYTE_INT_UNSIGNED);
                modbusMaster.setValue(locator, ((Number) value).intValue());
            } else {
                throw new IllegalArgumentException("不支持的寄存器类型: " + registerType + "，3=保持寄存器，4=输入寄存器");
            }
            return true;
        } catch (ModbusTransportException | ErrorResponseException e) {
            throw new IndustrialConnException("Modbus 写入寄存器失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean writeBatchValues(Map<String, Object> pointValueMap) {
        for (Map.Entry<String, Object> entry : pointValueMap.entrySet()) {
            writeSingleValue(entry.getKey(), entry.getValue());
        }
        return true;
    }

    @Override
    public void disconnect() {
        if (modbusMaster != null) {
            modbusMaster.destroy();
            modbusMaster = null;
        }
    }

    @Override
    public boolean isConnected() {
        return modbusMaster != null;
    }

    private void ensureConnected() {
        if (!isConnected()) {
            throw new IndustrialConnException("Modbus TCP 客户端未连接");
        }
    }

    private BaseLocator<?> parsePointId(String pointId) {
        String[] parts = pointId.split("_");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Modbus 点位格式错误，应为 slaveId_registerType_registerAddr，如 1_3_100");
        }
        int slaveId = Integer.parseInt(parts[0]);
        int registerType = Integer.parseInt(parts[1]);
        int registerAddr = Integer.parseInt(parts[2]);

        if (registerType == 3) {
            return BaseLocator.holdingRegister(slaveId, registerAddr, DataType.TWO_BYTE_INT_UNSIGNED);
        } else if (registerType == 4) {
            return BaseLocator.inputRegister(slaveId, registerAddr, DataType.TWO_BYTE_INT_UNSIGNED);
        } else {
            throw new IllegalArgumentException("不支持的寄存器类型: " + registerType + "，3=保持寄存器，4=输入寄存器");
        }
    }
}
