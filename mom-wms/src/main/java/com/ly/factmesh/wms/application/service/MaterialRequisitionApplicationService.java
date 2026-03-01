package com.ly.factmesh.wms.application.service;

import com.ly.factmesh.common.feign.dto.RequisitionCreateRequest;
import com.ly.factmesh.wms.domain.entity.Material;
import com.ly.factmesh.wms.domain.entity.MaterialRequisition;
import com.ly.factmesh.wms.domain.entity.MaterialRequisitionDetail;
import com.ly.factmesh.wms.domain.repository.MaterialRequisitionRepository;
import com.ly.factmesh.wms.domain.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 领料单应用服务（支持 MES 工单发布触发领料）
 *
 * @author LY-FactMesh
 */
@Service
@RequiredArgsConstructor
public class MaterialRequisitionApplicationService {

    private final MaterialRequisitionRepository requisitionRepository;
    private final MaterialRepository materialRepository;

    /**
     * 创建领料单（由 MES Feign 或 REST 调用）：按物料编码查找或创建物料，再创建领料单及明细
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createFromRequest(RequisitionCreateRequest request) {
        if (request.getWorkOrderId() == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("工单ID与领料数量必填且大于0");
        }
        String reqNo = "REQ-" + request.getWorkOrderId() + "-" + System.currentTimeMillis();
        MaterialRequisition req = new MaterialRequisition();
        req.setReqNo(reqNo);
        req.setOrderId(request.getWorkOrderId());
        req.setReqType(MaterialRequisition.REQ_TYPE_REQUISITION);
        req.setStatus(MaterialRequisition.STATUS_SUBMITTED);
        req.setCreateTime(LocalDateTime.now());
        req.setUpdateTime(LocalDateTime.now());
        MaterialRequisition saved = requisitionRepository.save(req);

        String materialCode = request.getMaterialCode() != null ? request.getMaterialCode() : "WO-" + request.getWorkOrderNo();
        String materialName = request.getMaterialName() != null ? request.getMaterialName() : "工单领料-" + request.getWorkOrderNo();
        Material material = materialRepository.findByMaterialCode(materialCode)
                .orElseGet(() -> {
                    Material m = new Material();
                    m.setMaterialCode(materialCode);
                    m.setMaterialName(materialName);
                    m.setCreateTime(LocalDateTime.now());
                    m.setUpdateTime(LocalDateTime.now());
                    return materialRepository.save(m);
                });

        MaterialRequisitionDetail detail = new MaterialRequisitionDetail();
        detail.setReqId(saved.getId());
        detail.setMaterialId(material.getId());
        detail.setQuantity(request.getQuantity());
        detail.setActualQuantity(0);
        requisitionRepository.saveDetail(detail);
        return saved.getId();
    }
}
