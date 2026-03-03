package com.ly.factmesh.infra.database;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 读写分离数据源配置
 * 当 infra.datasource.read-write-split.enabled=true 时生效
 * 主库写、从库读；使用 @ReadOnly 注解的方法路由到从库
 *
 * @author LY-FactMesh
 */
@Configuration
@ConditionalOnProperty(prefix = "infra.datasource.read-write-split", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(ReadWriteSplitProperties.class)
@Import(PostgresConfig.class)
public class DataSourceReadWriteSplitConfig {

    @Bean
    @Primary
    public DataSource dataSource(ReadWriteSplitProperties props, PostgresConfig postgresConfig) {
        String masterUrl = props.getMasterUrl() != null && !props.getMasterUrl().isBlank()
                ? props.getMasterUrl() : postgresConfig.getUrl();
        String slaveUrl = props.getSlaveUrl();
        if (slaveUrl == null || slaveUrl.isBlank()) {
            throw new IllegalStateException("读写分离已启用，需配置 infra.datasource.read-write-split.slave-url");
        }
        String username = props.getUsername() != null ? props.getUsername() : postgresConfig.getUsername();
        String password = props.getPassword() != null ? props.getPassword() : postgresConfig.getPassword();
        String driverClassName = props.getDriverClassName() != null ? props.getDriverClassName() : postgresConfig.getDriverClassName();
        int maxActive = props.getMaximumPoolSize() > 0 ? props.getMaximumPoolSize() : postgresConfig.getMaximumPoolSize();
        int minIdle = props.getMinimumIdle() > 0 ? props.getMinimumIdle() : postgresConfig.getMinimumIdle();
        long maxWait = props.getConnectionTimeout() > 0 ? props.getConnectionTimeout() : postgresConfig.getConnectionTimeout();

        DruidDataSource master = createDruidDataSource(masterUrl, username, password, driverClassName, maxActive, minIdle, maxWait);
        DruidDataSource slave = createDruidDataSource(slaveUrl, username, password, driverClassName, maxActive, minIdle, maxWait);

        DynamicDataSource dynamic = new DynamicDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceContextHolder.MASTER, master);
        targetDataSources.put(DataSourceContextHolder.SLAVE, slave);
        dynamic.setTargetDataSources(targetDataSources);
        dynamic.setDefaultTargetDataSource(master);
        dynamic.afterPropertiesSet();
        return dynamic;
    }

    @Bean
    public ReadOnlyAspect readOnlyAspect() {
        return new ReadOnlyAspect();
    }

    private static DruidDataSource createDruidDataSource(String url, String username, String password,
                                                         String driverClassName, int maxActive, int minIdle, long maxWait) {
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driverClassName);
        ds.setMaxActive(maxActive);
        ds.setMinIdle(minIdle);
        ds.setInitialSize(minIdle);
        ds.setMaxWait(maxWait);
        ds.setTimeBetweenEvictionRunsMillis(60000);
        ds.setMinEvictableIdleTimeMillis(300000);
        return ds;
    }
}
