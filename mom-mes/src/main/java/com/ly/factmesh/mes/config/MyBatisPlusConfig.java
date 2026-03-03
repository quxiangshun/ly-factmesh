package com.ly.factmesh.mes.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis-Plus 配置（兼容 Spring Boot 4）
 * 显式创建 SqlSessionFactory，配置分页插件
 *
 * @author LY-FactMesh
 */
@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, MybatisPlusInterceptor mybatisPlusInterceptor) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPlugins(mybatisPlusInterceptor);
        factory.setTypeAliasesPackage("com.ly.factmesh.mes.infrastructure.database.entity");
        var resources = new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*.xml");
        if (resources != null && resources.length > 0) {
            factory.setMapperLocations(resources);
        }
        return factory.getObject();
    }
}
