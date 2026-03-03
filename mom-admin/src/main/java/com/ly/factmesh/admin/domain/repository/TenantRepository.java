package com.ly.factmesh.admin.domain.repository;

import com.ly.factmesh.admin.domain.entity.Tenant;

import java.util.List;
import java.util.Optional;

/**
 * 租户仓储接口
 *
 * @author LY-FactMesh
 */
public interface TenantRepository {

    Tenant save(Tenant tenant);
    Optional<Tenant> findById(Long id);
    Optional<Tenant> findByTenantCode(String tenantCode);
    List<Tenant> findAll(long offset, long limit);
    long count();
    void deleteById(Long id);
}
