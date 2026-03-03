package com.ly.factmesh.mes.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.common.enums.ProductionLineStatusEnum;
import com.ly.factmesh.mes.domain.entity.ProductionLine;
import com.ly.factmesh.mes.domain.repository.ProductionLineRepository;
import com.ly.factmesh.mes.infrastructure.database.entity.ProductionLineEntity;
import com.ly.factmesh.mes.infrastructure.database.mapper.ProductionLineMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 产线仓储实现
 *
 * @author LY-FactMesh
 */
@Repository
@RequiredArgsConstructor
public class ProductionLineRepositoryImpl implements ProductionLineRepository {

    private final ProductionLineMapper productionLineMapper;

    @Override
    public ProductionLine save(ProductionLine productionLine) {
        ProductionLineEntity entity = toEntity(productionLine);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (entity.getId() == null) {
            entity.setId(SnowflakeIdGenerator.generateId());
            if (entity.getCreateTime() == null) entity.setCreateTime(now);
            if (entity.getUpdateTime() == null) entity.setUpdateTime(now);
            productionLineMapper.insert(entity);
        } else {
            entity.setUpdateTime(now);
            productionLineMapper.updateById(entity);
        }
        return toDomain(entity);
    }

    @Override
    public Optional<ProductionLine> findById(Long id) {
        ProductionLineEntity entity = productionLineMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public Optional<ProductionLine> findByLineCode(String lineCode) {
        LambdaQueryWrapper<ProductionLineEntity> q = new LambdaQueryWrapper<>();
        q.eq(ProductionLineEntity::getLineCode, lineCode);
        ProductionLineEntity entity = productionLineMapper.selectOne(q);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<ProductionLine> findAll(long offset, long limit) {
        Page<ProductionLineEntity> page = new Page<>(offset / limit + 1, limit);
        LambdaQueryWrapper<ProductionLineEntity> q = new LambdaQueryWrapper<>();
        q.orderByAsc(ProductionLineEntity::getSequence, ProductionLineEntity::getId);
        Page<ProductionLineEntity> result = productionLineMapper.selectPage(page, q);
        return result.getRecords().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return productionLineMapper.selectCount(null);
    }

    @Override
    public void deleteById(Long id) {
        productionLineMapper.deleteById(id);
    }

    private ProductionLineEntity toEntity(ProductionLine domain) {
        ProductionLineEntity e = new ProductionLineEntity();
        e.setId(domain.getId());
        e.setLineCode(domain.getLineCode());
        e.setLineName(domain.getLineName());
        e.setDescription(domain.getDescription());
        e.setSequence(domain.getSequence());
        e.setStatus(domain.getStatus() != null ? domain.getStatus() : ProductionLineStatusEnum.IDLE.getCode());
        e.setCreateTime(domain.getCreateTime());
        e.setUpdateTime(domain.getUpdateTime());
        return e;
    }

    private ProductionLine toDomain(ProductionLineEntity e) {
        ProductionLine d = new ProductionLine();
        d.setId(e.getId());
        d.setLineCode(e.getLineCode());
        d.setLineName(e.getLineName());
        d.setDescription(e.getDescription());
        d.setSequence(e.getSequence());
        d.setStatus(e.getStatus() != null ? e.getStatus() : ProductionLineStatusEnum.IDLE.getCode());
        d.setCreateTime(e.getCreateTime());
        d.setUpdateTime(e.getUpdateTime());
        return d;
    }
}
