package com.ly.factmesh.iot.infrastructure.protocol.pool;

import com.ly.factmesh.iot.infrastructure.protocol.api.IndustrialClient;
import com.ly.factmesh.iot.infrastructure.protocol.config.IndustrialClientConfig;
import com.ly.factmesh.iot.infrastructure.protocol.exception.IndustrialConnException;
import com.ly.factmesh.iot.infrastructure.protocol.factory.IndustrialClientFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * 工业客户端连接池，复用连接，避免频繁创建/销毁
 *
 * @author LY-FactMesh
 */
public class IndustrialClientConnectionPool {

    private final GenericObjectPool<IndustrialClient> pool;
    private final String protocolType;

    public IndustrialClientConnectionPool(String protocolType, IndustrialClientConfig config) {
        this(protocolType, config, defaultPoolConfig());
    }

    public IndustrialClientConnectionPool(String protocolType, IndustrialClientConfig config,
                                         GenericObjectPoolConfig<IndustrialClient> poolConfig) {
        this.protocolType = protocolType;
        this.pool = new GenericObjectPool<>(new ClientPooledFactory(protocolType, config), poolConfig);
    }

    /**
     * 从连接池获取客户端
     */
    public IndustrialClient borrowClient() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            throw new IndustrialConnException("从连接池获取客户端失败: " + e.getMessage(), e);
        }
    }

    /**
     * 归还客户端到连接池
     */
    public void returnClient(IndustrialClient client) {
        if (client != null) {
            pool.returnObject(client);
        }
    }

    /**
     * 销毁连接池
     */
    public void destroy() {
        pool.close();
    }

    public String getProtocolType() {
        return protocolType;
    }

    private static GenericObjectPoolConfig<IndustrialClient> defaultPoolConfig() {
        GenericObjectPoolConfig<IndustrialClient> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(10);
        config.setMaxIdle(5);
        config.setMinIdle(2);
        config.setTestOnBorrow(true);
        return config;
    }

    private static class ClientPooledFactory extends BasePooledObjectFactory<IndustrialClient> {
        private final String protocolType;
        private final IndustrialClientConfig config;

        ClientPooledFactory(String protocolType, IndustrialClientConfig config) {
            this.protocolType = protocolType;
            this.config = config;
        }

        @Override
        public IndustrialClient create() {
            return IndustrialClientFactory.createClient(protocolType, config);
        }

        @Override
        public PooledObject<IndustrialClient> wrap(IndustrialClient client) {
            return new DefaultPooledObject<>(client);
        }

        @Override
        public void destroyObject(PooledObject<IndustrialClient> p) {
            p.getObject().disconnect();
        }

        @Override
        public boolean validateObject(PooledObject<IndustrialClient> p) {
            return p.getObject().isConnected();
        }
    }
}
