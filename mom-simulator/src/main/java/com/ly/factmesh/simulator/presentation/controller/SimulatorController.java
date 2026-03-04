package com.ly.factmesh.simulator.presentation.controller;

import com.ly.factmesh.simulator.config.SimulatorProperties;
import com.ly.factmesh.simulator.config.SimulatorRuntimeConfig;
import com.ly.factmesh.simulator.simulator.ModbusTcpSimulator;
import com.ly.factmesh.simulator.simulator.OpcUaSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据模拟器 REST 接口
 * <p>提供 OPC UA、Modbus TCP 模拟点位状态与数值查询，支持界面配置更新间隔与设备数量</p>
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/simulator")
public class SimulatorController {

    private final SimulatorProperties properties;
    private final SimulatorRuntimeConfig runtimeConfig;
    private final OpcUaSimulator opcUaSimulator;
    private final ModbusTcpSimulator modbusTcpSimulator;

    public SimulatorController(SimulatorProperties properties,
                               SimulatorRuntimeConfig runtimeConfig,
                               @Autowired(required = false) OpcUaSimulator opcUaSimulator,
                               @Autowired(required = false) ModbusTcpSimulator modbusTcpSimulator) {
        this.properties = properties;
        this.runtimeConfig = runtimeConfig;
        this.opcUaSimulator = opcUaSimulator;
        this.modbusTcpSimulator = modbusTcpSimulator;
    }

    /**
     * 获取运行时配置（更新间隔、设备数量、选中设备 ID）
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("intervalMs", runtimeConfig.getIntervalMs());
        result.put("deviceCount", runtimeConfig.getDeviceCount());
        result.put("deviceIds", runtimeConfig.getDeviceIds());
        return ResponseEntity.ok(result);
    }

    /**
     * 更新运行时配置（不传则保持当前值）
     * deviceIds：选中的设备 ID 列表，优先于 deviceCount
     */
    @PutMapping("/config")
    public ResponseEntity<Map<String, Object>> updateConfig(@RequestBody Map<String, Object> body) {
        if (body.containsKey("intervalMs")) {
            Object v = body.get("intervalMs");
            int ms = v instanceof Number ? ((Number) v).intValue() : Integer.parseInt(String.valueOf(v));
            runtimeConfig.setIntervalMs(ms);
        }
        if (body.containsKey("deviceIds")) {
            @SuppressWarnings("unchecked")
            List<?> raw = (List<?>) body.get("deviceIds");
            List<String> ids = raw == null ? new ArrayList<>() : raw.stream()
                    .map(o -> o != null ? String.valueOf(o) : null)
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.toList());
            runtimeConfig.setDeviceIds(ids);
        } else if (body.containsKey("deviceCount")) {
            Object v = body.get("deviceCount");
            int count = v instanceof Number ? ((Number) v).intValue() : Integer.parseInt(String.valueOf(v));
            runtimeConfig.setDeviceCount(count);
            runtimeConfig.setDeviceIds(null);
        }
        return getConfig();
    }

    /**
     * 获取模拟器整体状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("opcua", Map.of(
            "enabled", properties.getOpcua().isEnabled(),
            "running", opcUaSimulator != null && opcUaSimulator.isRunning(),
            "endpoint", "opc.tcp://" + (properties.getOpcua().getHost().equals("0.0.0.0") ? "localhost" : properties.getOpcua().getHost()) + ":" + properties.getOpcua().getPort(),
            "intervalMs", runtimeConfig.getIntervalMs(),
            "deviceCount", runtimeConfig.getDeviceCount(),
            "note", "内存模拟模式，实际 OPC UA 连接请使用 Prosys 等工具"
        ));
        result.put("modbus", Map.of(
            "enabled", properties.getModbus().isEnabled(),
            "running", modbusTcpSimulator != null && modbusTcpSimulator.isRunning(),
            "host", properties.getModbus().getHost(),
            "port", properties.getModbus().getPort(),
            "intervalMs", runtimeConfig.getIntervalMs(),
            "deviceCount", runtimeConfig.getDeviceCount(),
            "note", "内存模拟模式，实际 Modbus TCP 连接请使用 Modbus Slave 等工具"
        ));
        return ResponseEntity.ok(result);
    }

    /**
     * 获取 OPC UA 模拟点位当前值
     */
    @GetMapping("/opcua/values")
    public ResponseEntity<Map<String, Object>> getOpcUaValues() {
        if (opcUaSimulator == null) {
            return ResponseEntity.ok(Map.of("error", "OPC UA 模拟器未启用"));
        }
        Map<String, Object> values = opcUaSimulator.getAllValues().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
        return ResponseEntity.ok(values);
    }

    /**
     * 获取 Modbus TCP 模拟点位当前值
     */
    @GetMapping("/modbus/values")
    public ResponseEntity<Map<String, Object>> getModbusValues() {
        if (modbusTcpSimulator == null) {
            return ResponseEntity.ok(Map.of("error", "Modbus TCP 模拟器未启用"));
        }
        Map<String, Object> values = modbusTcpSimulator.getAllValues().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
        return ResponseEntity.ok(values);
    }

    /**
     * 获取全部模拟点位当前值（汇总）
     */
    @GetMapping("/values")
    public ResponseEntity<Map<String, Object>> getAllValues() {
        Map<String, Object> result = new HashMap<>();
        if (opcUaSimulator != null) {
            Map<String, Object> opcuaValues = opcUaSimulator.getAllValues().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
            result.put("opcua", opcuaValues);
        }
        if (modbusTcpSimulator != null) {
            Map<String, Object> modbusValues = modbusTcpSimulator.getAllValues().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
            result.put("modbus", modbusValues);
        }
        return ResponseEntity.ok(result);
    }
}
