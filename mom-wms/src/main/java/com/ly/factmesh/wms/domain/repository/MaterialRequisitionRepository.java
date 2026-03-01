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

    Optional<MaterialRequisition> findById(Long id);

    Optional<MaterialRequisition> findByReqNo(String reqNo);
}
