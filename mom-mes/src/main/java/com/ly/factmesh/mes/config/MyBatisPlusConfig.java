package com.ly.factmesh.mes.config;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * MyBatis-Plus 配置（兼容 Spring Boot 4）
 * 显式创建 SqlSessionFactory，确保 Mapper 可正确注入
 *
 * @author LY-FactMesh
 */
@Configuration
public class MyBatisPlusConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTypeAliasesPackage("com.ly.factmesh.mes.infrastructure.database.entity");
        var resources = new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*.xml");
        if (resources != null && resources.length > 0) {
            factory.setMapperLocations(resources);
        }
        return factory.getObject();
    }
}
