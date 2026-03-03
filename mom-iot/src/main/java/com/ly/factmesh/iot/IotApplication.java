package com.ly.factmesh.iot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IoT模块应用入口类
 *
 * @author 屈想顺
 */
@SpringBootApplication(exclude = {
    com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
})
@MapperScan("com.ly.factmesh.iot.infrastructure.repository")
@EnableScheduling
public class IotApplication {
    
    /**
     * 应用入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(IotApplication.class, args);
    }
}