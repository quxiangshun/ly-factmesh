package com.ly.factmesh.wms.domain.repository;

import com.ly.factmesh.wms.domain.entity.Material;

import java.util.List;
import java.util.Optional;

/**
 * 物料仓储接口
 *
 * @author LY-FactMesh
 */
public interface MaterialRepository {

    Material save(Material material);

    Optional<Material> findById(Long id);

    Optional<Material> findByMaterialCode(String materialCode);

    List<Material> findAll(long offset, long limit);

    long count();

    void deleteById(Long id);
}
