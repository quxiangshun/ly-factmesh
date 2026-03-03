package com.ly.factmesh.mes.domain.repository;

import com.ly.factmesh.mes.domain.entity.WorkOrder;

import java.util.List;
import java.util.Optional;

/**
 * 工单仓储接口
 *
 * @author LY-FactMesh
 */
public interface WorkOrderRepository {

    WorkOrder save(WorkOrder workOrder);

    Optional<WorkOrder> findById(Long id);

    Optional<WorkOrder> findByOrderCode(String orderCode);

    List<WorkOrder> findAll(long offset, long limit);

    long count();

    long countByStatus(Integer status);

    void deleteById(Long id);
}
