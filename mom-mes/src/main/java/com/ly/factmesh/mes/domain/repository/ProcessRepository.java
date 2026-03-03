package com.ly.factmesh.mes.domain.repository;

import com.ly.factmesh.mes.domain.entity.Process;

import java.util.List;
import java.util.Optional;

/**
 * 工序仓储接口
 *
 * @author LY-FactMesh
 */
public interface ProcessRepository {

    Process save(Process process);
    Optional<Process> findById(Long id);
    Optional<Process> findByProcessCode(String processCode);
    List<Process> findAll(long offset, long limit);
    long count();
    void deleteById(Long id);
}
