package com.ly.factmesh.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 数据模拟器应用入口
 * <p>OPC UA、Modbus TCP 模拟服务端，仅用于开发环境联调，生产环境不部署</p>
 *
 * @author LY-FactMesh
 */
@SpringBootApplication(scanBasePackages = "com.ly.factmesh.simulator")
@ComponentScan(basePackages = "com.ly.factmesh.simulator")
public class SimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulatorApplication.class, args);
    }
}
