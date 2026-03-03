package com.ly.factmesh.mes.domain.repository;

import com.ly.factmesh.mes.domain.entity.ProductionLine;

import java.util.List;
import java.util.Optional;

/**
 * 产线仓储接口
 *
 * @author LY-FactMesh
 */
public interface ProductionLineRepository {

    ProductionLine save(ProductionLine productionLine);
    Optional<ProductionLine> findById(Long id);
    Optional<ProductionLine> findByLineCode(String lineCode);
    List<ProductionLine> findAll(long offset, long limit);
    long count();
    void deleteById(Long id);
}
