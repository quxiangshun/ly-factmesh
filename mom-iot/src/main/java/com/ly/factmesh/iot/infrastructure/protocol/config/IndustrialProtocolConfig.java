package com.ly.factmesh.iot.infrastructure.protocol.config;

import com.ly.factmesh.iot.infrastructure.protocol.factory.IndustrialClientFactory;
import com.ly.factmesh.iot.infrastructure.protocol.pool.IndustrialClientConnectionPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工业协议（OPC UA、Modbus TCP）配置
 * 当 iot.industrial.enabled=true 时生效
 *
 * @author LY-FactMesh
 */
@Configuration
@EnableConfigurationProperties(IndustrialProtocolProperties.class)
@ConditionalOnProperty(prefix = "iot.industrial", name = "enabled", havingValue = "true")
public class IndustrialProtocolConfig {

    @Bean("opcUaConnectionPool")
    public IndustrialClientConnectionPool opcUaConnectionPool(IndustrialProtocolProperties props) {
        IndustrialClientConfig config = new IndustrialClientConfig();
        config.setEndpointUrl(props.getOpcua().getEndpointUrl());
        config.setUsername(props.getOpcua().getUsername());
        config.setPassword(props.getOpcua().getPassword());
        config.setConnectTimeoutMs(props.getOpcua().getConnectTimeoutMs());

        GenericObjectPoolConfig<com.ly.factmesh.iot.infrastructure.protocol.api.IndustrialClient> poolConfig =
                new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(props.getOpcua().getPoolMaxTotal());
        poolConfig.setMaxIdle(props.getOpcua().getPoolMaxIdle());
        poolConfig.setMinIdle(props.getOpcua().getPoolMinIdle());
        poolConfig.setTestOnBorrow(true);

        return new IndustrialClientConnectionPool(IndustrialClientFactory.OPC_UA, config, poolConfig);
    }

    @Bean("modbusConnectionPool")
    public IndustrialClientConnectionPool modbusConnectionPool(IndustrialProtocolProperties props) {
        IndustrialClientConfig config = new IndustrialClientConfig();
        config.setHost(props.getModbus().getHost());
        config.setPort(props.getModbus().getPort());
        config.setConnectTimeoutMs(props.getModbus().getConnectTimeoutMs());
        config.setRetryCount(props.getModbus().getRetryCount());

        GenericObjectPoolConfig<com.ly.factmesh.iot.infrastructure.protocol.api.IndustrialClient> poolConfig =
                new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(props.getModbus().getPoolMaxTotal());
        poolConfig.setMaxIdle(props.getModbus().getPoolMaxIdle());
        poolConfig.setMinIdle(props.getModbus().getPoolMinIdle());
        poolConfig.setTestOnBorrow(true);

        return new IndustrialClientConnectionPool(IndustrialClientFactory.MODBUS_TCP, config, poolConfig);
    }
}
