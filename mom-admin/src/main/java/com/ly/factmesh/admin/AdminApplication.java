package com.ly.factmesh.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 系统管理模块应用入口类
 *
 * @author 屈想顺
 */
@SpringBootApplication(
    scanBasePackages = "com.ly.factmesh",
    exclude = { com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class }
)
@MapperScan("com.ly.factmesh.admin.infrastructure.database.mapper")
public class AdminApplication {
    
    /**
     * 应用入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}