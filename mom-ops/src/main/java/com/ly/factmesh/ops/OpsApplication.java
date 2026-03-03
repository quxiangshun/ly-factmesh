package com.ly.factmesh.ops;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 运维模块应用入口
 * <p>系统级事件、审计记录、全局日志等运维数据管理</p>
 *
 * @author LY-FactMesh
 */
@SpringBootApplication(
    scanBasePackages = "com.ly.factmesh",
    exclude = { com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class }
)
@MapperScan("com.ly.factmesh.ops.infrastructure.database.mapper")
public class OpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpsApplication.class, args);
    }
}
