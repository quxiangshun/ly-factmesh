package com.ly.factmesh.wms.domain.repository;

import com.ly.factmesh.wms.domain.entity.InventoryTransaction;

import java.util.List;

/**
 * 出入库记录仓储接口
 *
 * @author LY-FactMesh
 */
public interface InventoryTransactionRepository {

    InventoryTransaction save(InventoryTransaction transaction);

    List<InventoryTransaction> findByMaterialId(Long materialId, long offset, long limit);

    long countByMaterialId(Long materialId);
}
