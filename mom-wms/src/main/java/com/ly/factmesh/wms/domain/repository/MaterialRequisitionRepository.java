package com.ly.factmesh.wms.domain.repository;

import com.ly.factmesh.wms.domain.entity.MaterialRequisition;
import com.ly.factmesh.wms.domain.entity.MaterialRequisitionDetail;

import java.util.List;
import java.util.Optional;

/**
 * 领料单仓储接口
 *
 * @author LY-FactMesh
 */
public interface MaterialRequisitionRepository {

    MaterialRequisition save(MaterialRequisition req);

    void saveDetail(MaterialRequisitionDetail detail);

    void updateDetail(MaterialRequisitionDetail detail);

    Optional<MaterialRequisition> findById(Long id);

    Optional<MaterialRequisition> findByReqNo(String reqNo);

    List<MaterialRequisition> findAll(long offset, long limit, Long orderId, Integer status);

    long count(Long orderId, Integer status);

    List<MaterialRequisitionDetail> findDetailsByReqId(Long reqId);

    boolean existsDetailByMaterialId(Long materialId);
}
