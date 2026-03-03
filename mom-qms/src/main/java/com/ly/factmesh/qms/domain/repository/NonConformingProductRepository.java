package com.ly.factmesh.qms.domain.repository;

import com.ly.factmesh.qms.domain.entity.NonConformingProduct;

import java.util.List;
import java.util.Optional;

/**
 * 不合格品仓储接口
 *
 * @author LY-FactMesh
 */
public interface NonConformingProductRepository {

    NonConformingProduct save(NonConformingProduct product);

    Optional<NonConformingProduct> findById(Long id);

    List<NonConformingProduct> findAll(long offset, long limit, Integer disposalResult, Long taskId);

    List<NonConformingProduct> findByTaskId(Long taskId);

    long countByDisposalResult(Integer disposalResult);

    long countByDisposalResultAndTaskId(Integer disposalResult, Long taskId);

    Optional<NonConformingProduct> findByNcrNo(String ncrNo);

    void deleteById(Long id);
}
