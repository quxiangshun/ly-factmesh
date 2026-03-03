package com.ly.factmesh.wms.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.wms.domain.entity.Inventory;
import com.ly.factmesh.wms.domain.repository.InventoryRepository;
import com.ly.factmesh.wms.infrastructure.database.entity.InventoryEntity;
import com.ly.factmesh.wms.infrastructure.database.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {

    private final InventoryMapper inventoryMapper;

    @Override
    public Inventory save(Inventory inventory) {
        InventoryEntity e = toEntity(inventory);
        LocalDateTime now = LocalDateTime.now();
        if (e.getId() == null) {
            e.setId(SnowflakeIdGenerator.generateId());
            e.setLastUpdateTime(now);
            inventoryMapper.insert(e);
        } else {
            e.setLastUpdateTime(now);
            inventoryMapper.updateById(e);
        }
        return toDomain(e);
    }

    @Override
    public Optional<Inventory> findById(Long id) {
        InventoryEntity e = inventoryMapper.selectById(id);
        return Optional.ofNullable(e).map(this::toDomain);
    }

    @Override
    public Optional<Inventory> findByMaterialAndLocation(Long materialId, String warehouse, String location) {
        return findByMaterialAndLocation(materialId, warehouse, location, null);
    }

    @Override
    public Optional<Inventory> findByMaterialAndLocation(Long materialId, String warehouse, String location, String batchNo) {
        LambdaQueryWrapper<InventoryEntity> q = new LambdaQueryWrapper<>();
        q.eq(InventoryEntity::getMaterialId, materialId);
        String wh = (warehouse == null || warehouse.isBlank()) ? "" : warehouse;
        String loc = (location == null || location.isBlank()) ? "" : location;
        String batch = (batchNo == null || batchNo.isBlank()) ? null : batchNo;
        if (wh.isEmpty()) {
            q.and(w -> w.isNull(InventoryEntity::getWarehouse).or().eq(InventoryEntity::getWarehouse, ""));
        } else {
            q.eq(InventoryEntity::getWarehouse, wh);
        }
        if (loc.isEmpty()) {
            q.and(w -> w.isNull(InventoryEntity::getLocation).or().eq(InventoryEntity::getLocation, ""));
        } else {
            q.eq(InventoryEntity::getLocation, loc);
        }
        if (batch == null) {
            q.and(w -> w.isNull(InventoryEntity::getBatchNo).or().eq(InventoryEntity::getBatchNo, ""));
        } else {
            q.eq(InventoryEntity::getBatchNo, batch);
        }
        InventoryEntity e = inventoryMapper.selectOne(q);
        return Optional.ofNullable(e).map(this::toDomain);
    }

    @Override
    public List<Inventory> findByMaterialId(Long materialId) {
        if (materialId == null) return List.of();
        LambdaQueryWrapper<InventoryEntity> q = new LambdaQueryWrapper<>();
        q.eq(InventoryEntity::getMaterialId, materialId);
        return inventoryMapper.selectList(q).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Inventory> findAll(long offset, long limit, Long materialId, String warehouse, String batchNo) {
        long pageNum = limit <= 0 ? 1 : offset / limit + 1;
        Page<InventoryEntity> page = new Page<>(pageNum, limit);
        LambdaQueryWrapper<InventoryEntity> q = new LambdaQueryWrapper<>();
        if (materialId != null) q.eq(InventoryEntity::getMaterialId, materialId);
        if (warehouse != null && !warehouse.isBlank()) q.eq(InventoryEntity::getWarehouse, warehouse);
        if (batchNo != null && !batchNo.isBlank()) q.eq(InventoryEntity::getBatchNo, batchNo);
        q.orderByDesc(InventoryEntity::getLastUpdateTime);
        return inventoryMapper.selectPage(page, q).getRecords().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count(Long materialId, String warehouse, String batchNo) {
        LambdaQueryWrapper<InventoryEntity> q = new LambdaQueryWrapper<>();
        if (materialId != null) q.eq(InventoryEntity::getMaterialId, materialId);
        if (warehouse != null && !warehouse.isBlank()) q.eq(InventoryEntity::getWarehouse, warehouse);
        if (batchNo != null && !batchNo.isBlank()) q.eq(InventoryEntity::getBatchNo, batchNo);
        return inventoryMapper.selectCount(q);
    }

    @Override
    public List<Inventory> findAllBelowSafeStock(long offset, long limit) {
        long pageNum = limit <= 0 ? 1 : offset / limit + 1;
        Page<InventoryEntity> page = new Page<>(pageNum, limit);
        LambdaQueryWrapper<InventoryEntity> q = new LambdaQueryWrapper<>();
        q.apply("safe_stock > 0 AND (quantity IS NULL OR quantity < safe_stock)");
        q.orderByAsc(InventoryEntity::getQuantity);
        return inventoryMapper.selectPage(page, q).getRecords().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countBelowSafeStock() {
        LambdaQueryWrapper<InventoryEntity> q = new LambdaQueryWrapper<>();
        q.apply("safe_stock > 0 AND (quantity IS NULL OR quantity < safe_stock)");
        return inventoryMapper.selectCount(q);
    }

    private InventoryEntity toEntity(Inventory d) {
        InventoryEntity e = new InventoryEntity();
        e.setId(d.getId());
        e.setMaterialId(d.getMaterialId());
        e.setBatchNo(d.getBatchNo());
        e.setWarehouse(d.getWarehouse());
        e.setLocation(d.getLocation());
        e.setQuantity(d.getQuantity() != null ? d.getQuantity() : 0);
        e.setSafeStock(d.getSafeStock() != null ? d.getSafeStock() : 0);
        e.setLastUpdateTime(d.getLastUpdateTime());
        return e;
    }

    private Inventory toDomain(InventoryEntity e) {
        Inventory d = new Inventory();
        d.setId(e.getId());
        d.setMaterialId(e.getMaterialId());
        d.setBatchNo(e.getBatchNo());
        d.setWarehouse(e.getWarehouse());
        d.setLocation(e.getLocation());
        d.setQuantity(e.getQuantity());
        d.setSafeStock(e.getSafeStock());
        d.setLastUpdateTime(e.getLastUpdateTime());
        return d;
    }
}
