package com.ly.factmesh.mes.domain.repository;

import com.ly.factmesh.mes.domain.entity.WorkReport;

import java.util.List;
import java.util.Optional;

/**
 * 报工记录仓储接口
 *
 * @author LY-FactMesh
 */
public interface WorkReportRepository {

    WorkReport save(WorkReport workReport);
    Optional<WorkReport> findById(Long id);
    List<WorkReport> findByOrderId(Long orderId, long offset, long limit);
    List<WorkReport> findAll(long offset, long limit);
    long count();
    long countByOrderId(Long orderId);
    void deleteById(Long id);
}
