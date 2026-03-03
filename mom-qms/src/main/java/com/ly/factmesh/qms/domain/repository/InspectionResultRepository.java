package com.ly.factmesh.qms.domain.repository;

import com.ly.factmesh.qms.domain.entity.InspectionResult;

import java.util.List;
import java.util.Optional;

/**
 * 质检结果仓储接口
 *
 * @author LY-FactMesh
 */
public interface InspectionResultRepository {

    InspectionResult save(InspectionResult result);

    Optional<InspectionResult> findById(Long id);

    List<InspectionResult> findByTaskId(Long taskId);

    void deleteById(Long id);
}
