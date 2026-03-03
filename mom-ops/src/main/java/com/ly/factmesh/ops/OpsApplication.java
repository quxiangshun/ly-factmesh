package com.ly.factmesh.ops;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.ly.factmesh.infra.database.MyBatisPlusConfig;

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
@ComponentScan(
    basePackages = "com.ly.factmesh",
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MyBatisPlusConfig.class)
)
@MapperScan("com.ly.factmesh.ops.infrastructure.database.mapper")
public class OpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpsApplication.class, args);
    }
}
