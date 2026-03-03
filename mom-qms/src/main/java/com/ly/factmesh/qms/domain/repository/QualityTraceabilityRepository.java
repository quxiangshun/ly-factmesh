package com.ly.factmesh.qms.domain.repository;

import com.ly.factmesh.qms.domain.entity.QualityTraceability;

import java.util.List;

/**
 * 质量追溯仓储接口
 *
 * @author LY-FactMesh
 */
public interface QualityTraceabilityRepository {

    QualityTraceability save(QualityTraceability trace);

    List<QualityTraceability> findByProductCode(String productCode);

    List<QualityTraceability> findByBatchNo(String batchNo);

    List<QualityTraceability> findByProductionOrder(String productionOrder);

    List<QualityTraceability> findByProductCodeAndBatchNo(String productCode, String batchNo);

    List<QualityTraceability> findTrace(String productCode, String batchNo, String productionOrder);
}
