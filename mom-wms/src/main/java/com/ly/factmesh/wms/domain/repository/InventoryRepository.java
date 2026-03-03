package com.ly.factmesh.wms.domain.repository;

import com.ly.factmesh.wms.domain.entity.Inventory;

import java.util.List;
import java.util.Optional;

/**
 * 库存仓储接口
 *
 * @author LY-FactMesh
 */
public interface InventoryRepository {

    Inventory save(Inventory inventory);

    Optional<Inventory> findById(Long id);

    Optional<Inventory> findByMaterialAndLocation(Long materialId, String warehouse, String location);

    List<Inventory> findByMaterialId(Long materialId);

    List<Inventory> findAll(long offset, long limit, Long materialId, String warehouse);

    long count(Long materialId, String warehouse);

    List<Inventory> findAllBelowSafeStock(long offset, long limit);

    long countBelowSafeStock();
}
