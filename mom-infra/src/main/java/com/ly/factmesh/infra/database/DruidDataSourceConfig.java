package com.ly.factmesh.infra.database;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

/**
 * Druid数据源配置
 * 
 * @author 屈想顺
 */
@Configuration
@Import(PostgresConfig.class)
public class DruidDataSourceConfig {
    
    @Bean
    public DataSource dataSource(PostgresConfig postgresConfig) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(postgresConfig.getUrl());
        dataSource.setUsername(postgresConfig.getUsername());
        dataSource.setPassword(postgresConfig.getPassword());
        dataSource.setDriverClassName(postgresConfig.getDriverClassName());
        dataSource.setMaxActive(postgresConfig.getMaximumPoolSize());
        dataSource.setMinIdle(postgresConfig.getMinimumIdle());
        dataSource.setInitialSize(postgresConfig.getMinimumIdle());
        dataSource.setMaxWait(postgresConfig.getConnectionTimeout());
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(postgresConfig.getIdleTimeout());
        return dataSource;
    }
}
