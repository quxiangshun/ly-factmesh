package com.ly.factmesh.wms.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.wms.domain.entity.Material;
import com.ly.factmesh.wms.domain.repository.MaterialRepository;
import com.ly.factmesh.wms.infrastructure.database.entity.MaterialEntity;
import com.ly.factmesh.wms.infrastructure.database.mapper.MaterialMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MaterialRepositoryImpl implements MaterialRepository {

    private final MaterialMapper materialMapper;

    @Override
    public Material save(Material material) {
        MaterialEntity entity = toEntity(material);
        LocalDateTime now = LocalDateTime.now();
        if (entity.getId() == null) {
            entity.setId(SnowflakeIdGenerator.generateId());
            if (entity.getCreateTime() == null) entity.setCreateTime(now);
            if (entity.getUpdateTime() == null) entity.setUpdateTime(now);
            materialMapper.insert(entity);
        } else {
            entity.setUpdateTime(now);
            materialMapper.updateById(entity);
        }
        return toDomain(entity);
    }

    @Override
    public Optional<Material> findById(Long id) {
        MaterialEntity entity = materialMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public Optional<Material> findByMaterialCode(String materialCode) {
        LambdaQueryWrapper<MaterialEntity> q = new LambdaQueryWrapper<>();
        q.eq(MaterialEntity::getMaterialCode, materialCode);
        MaterialEntity entity = materialMapper.selectOne(q);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<Material> findAll(long offset, long limit) {
        long pageNum = limit <= 0 ? 1 : offset / limit + 1;
        Page<MaterialEntity> page = new Page<>(pageNum, limit);
        Page<MaterialEntity> result = materialMapper.selectPage(page, null);
        return result.getRecords().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return materialMapper.selectCount(null);
    }

    @Override
    public void deleteById(Long id) {
        materialMapper.deleteById(id);
    }

    private MaterialEntity toEntity(Material domain) {
        MaterialEntity e = new MaterialEntity();
        e.setId(domain.getId());
        e.setMaterialCode(domain.getMaterialCode());
        e.setMaterialName(domain.getMaterialName());
        e.setMaterialType(domain.getMaterialType());
        e.setSpecification(domain.getSpecification());
        e.setUnit(domain.getUnit());
        e.setCreateTime(domain.getCreateTime());
        e.setUpdateTime(domain.getUpdateTime());
        return e;
    }

    private Material toDomain(MaterialEntity e) {
        Material d = new Material();
        d.setId(e.getId());
        d.setMaterialCode(e.getMaterialCode());
        d.setMaterialName(e.getMaterialName());
        d.setMaterialType(e.getMaterialType());
        d.setSpecification(e.getSpecification());
        d.setUnit(e.getUnit());
        d.setCreateTime(e.getCreateTime());
        d.setUpdateTime(e.getUpdateTime());
        return d;
    }
}
