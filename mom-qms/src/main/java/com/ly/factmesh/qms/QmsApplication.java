package com.ly.factmesh.qms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 质量管理系统模块应用入口类
 *
 * @author 屈想顺
 */
@SpringBootApplication
@MapperScan("com.ly.factmesh.qms.infrastructure.database.mapper")
public class QmsApplication {
    
    /**
     * 应用入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(QmsApplication.class, args);
    }
}