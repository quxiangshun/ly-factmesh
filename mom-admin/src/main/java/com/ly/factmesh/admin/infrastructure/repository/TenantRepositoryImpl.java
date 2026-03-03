package com.ly.factmesh.admin.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.domain.entity.Tenant;
import com.ly.factmesh.admin.domain.repository.TenantRepository;
import com.ly.factmesh.admin.infrastructure.database.entity.TenantEntity;
import com.ly.factmesh.admin.infrastructure.database.mapper.TenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 租户仓储实现
 *
 * @author LY-FactMesh
 */
@Repository
@RequiredArgsConstructor
public class TenantRepositoryImpl implements TenantRepository {

    private final TenantMapper tenantMapper;

    @Override
    public Tenant save(Tenant tenant) {
        TenantEntity e = toEntity(tenant);
        LocalDateTime now = LocalDateTime.now();
        if (e.getId() == null) {
            e.setCreateTime(now);
            e.setUpdateTime(now);
            tenantMapper.insert(e);
        } else {
            e.setUpdateTime(now);
            tenantMapper.updateById(e);
        }
        return toDomain(tenantMapper.selectById(e.getId()));
    }

    @Override
    public Optional<Tenant> findById(Long id) {
        return Optional.ofNullable(tenantMapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public Optional<Tenant> findByTenantCode(String tenantCode) {
        LambdaQueryWrapper<TenantEntity> q = new LambdaQueryWrapper<>();
        q.eq(TenantEntity::getTenantCode, tenantCode);
        return Optional.ofNullable(tenantMapper.selectOne(q)).map(this::toDomain);
    }

    @Override
    public List<Tenant> findAll(long offset, long limit) {
        Page<TenantEntity> page = new Page<>(offset / limit + 1, limit);
        LambdaQueryWrapper<TenantEntity> q = new LambdaQueryWrapper<>();
        q.orderByAsc(TenantEntity::getId);
        return tenantMapper.selectPage(page, q).getRecords().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return tenantMapper.selectCount(null);
    }

    @Override
    public void deleteById(Long id) {
        tenantMapper.deleteById(id);
    }

    private TenantEntity toEntity(Tenant d) {
        TenantEntity e = new TenantEntity();
        e.setId(d.getId());
        e.setTenantCode(d.getTenantCode());
        e.setTenantName(d.getTenantName());
        e.setContact(d.getContact());
        e.setPhone(d.getPhone());
        e.setStatus(d.getStatus());
        e.setConfig(d.getConfig());
        e.setCreateTime(d.getCreateTime());
        e.setUpdateTime(d.getUpdateTime());
        return e;
    }

    private Tenant toDomain(TenantEntity e) {
        if (e == null) return null;
        Tenant d = new Tenant();
        d.setId(e.getId());
        d.setTenantCode(e.getTenantCode());
        d.setTenantName(e.getTenantName());
        d.setContact(e.getContact());
        d.setPhone(e.getPhone());
        d.setStatus(e.getStatus());
        d.setConfig(e.getConfig());
        d.setCreateTime(e.getCreateTime());
        d.setUpdateTime(e.getUpdateTime());
        return d;
    }
}
