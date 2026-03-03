package com.ly.factmesh.infra.database;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源（读写分离路由）
 *
 * @author LY-FactMesh
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        String key = DataSourceContextHolder.get();
        return key != null ? key : DataSourceContextHolder.MASTER;
    }
}
