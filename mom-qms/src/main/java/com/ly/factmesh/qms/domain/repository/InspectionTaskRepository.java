package com.ly.factmesh.qms.domain.repository;

import com.ly.factmesh.qms.domain.entity.InspectionTask;

import java.util.List;
import java.util.Optional;

/**
 * 质检任务仓储接口
 *
 * @author LY-FactMesh
 */
public interface InspectionTaskRepository {

    InspectionTask save(InspectionTask task);

    Optional<InspectionTask> findById(Long id);

    Optional<InspectionTask> findByTaskCode(String taskCode);

    List<InspectionTask> findAll(long offset, long limit);

    long count();

    void deleteById(Long id);
}
