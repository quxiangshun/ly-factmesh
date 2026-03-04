package com.ly.factmesh.simulator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 模拟器配置属性（绑定 yml 中的 simulator 节点）
 *
 * @author LY-FactMesh
 */
@Data
@Component
@ConfigurationProperties(prefix = "simulator")
public class SimulatorProperties {

    private OpcUaConfig opcua = new OpcUaConfig();
    private ModbusConfig modbus = new ModbusConfig();

    @Data
    public static class OpcUaConfig {
        private boolean enabled = true;
        private String host = "0.0.0.0";
        private int port = 4840;
    }

    @Data
    public static class ModbusConfig {
        private boolean enabled = true;
        private String host = "0.0.0.0";
        private int port = 502;
    }
}
