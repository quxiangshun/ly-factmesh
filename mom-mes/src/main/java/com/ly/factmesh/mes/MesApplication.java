package com.ly.factmesh.mes;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 制造执行系统模块应用入口类
 *
 * @author 屈想顺
 */
@SpringBootApplication
@MapperScan("com.ly.factmesh.mes.infrastructure.database.mapper")
@EnableFeignClients(basePackages = "com.ly.factmesh.common.feign")
public class MesApplication {
    
    /**
     * 应用入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MesApplication.class, args);
    }
}