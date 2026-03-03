package com.ly.factmesh.wms.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.feign.dto.RequisitionCreateRequest;
import com.ly.factmesh.wms.application.dto.MaterialRequisitionDetailDTO;
import com.ly.factmesh.wms.application.dto.MaterialRequisitionDTO;
import com.ly.factmesh.wms.application.dto.RequisitionCompleteRequest;
import com.ly.factmesh.wms.application.dto.RequisitionManualCreateRequest;
import com.ly.factmesh.wms.domain.entity.Material;
import com.ly.factmesh.wms.domain.entity.MaterialRequisition;
import com.ly.factmesh.wms.domain.entity.MaterialRequisitionDetail;
import com.ly.factmesh.wms.domain.repository.MaterialRequisitionRepository;
import com.ly.factmesh.wms.domain.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final InventoryApplicationService inventoryApplicationService;

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

    /**
     * 手动创建领料单（草稿状态）
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createDraft(RequisitionManualCreateRequest request) {
        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new IllegalArgumentException("物料不存在: " + request.getMaterialId()));
        String reqNo = "REQ-M-" + System.currentTimeMillis();
        MaterialRequisition req = new MaterialRequisition();
        req.setReqNo(reqNo);
        req.setOrderId(request.getOrderId());
        req.setReqType(request.getReqType() != null ? request.getReqType() : MaterialRequisition.REQ_TYPE_REQUISITION);
        req.setStatus(MaterialRequisition.STATUS_DRAFT);
        req.setCreateTime(LocalDateTime.now());
        req.setUpdateTime(LocalDateTime.now());
        MaterialRequisition saved = requisitionRepository.save(req);

        MaterialRequisitionDetail detail = new MaterialRequisitionDetail();
        detail.setReqId(saved.getId());
        detail.setMaterialId(material.getId());
        detail.setQuantity(request.getQuantity());
        detail.setActualQuantity(0);
        requisitionRepository.saveDetail(detail);
        return saved.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        MaterialRequisition req = requisitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("领料单不存在: " + id));
        if (req.getStatus() == MaterialRequisition.STATUS_DONE) {
            throw new IllegalArgumentException("已完成的领料单不能取消");
        }
        if (req.getStatus() == MaterialRequisition.STATUS_CANCELLED) {
            throw new IllegalArgumentException("领料单已取消");
        }
        req.setStatus(MaterialRequisition.STATUS_CANCELLED);
        req.setUpdateTime(LocalDateTime.now());
        requisitionRepository.save(req);
    }

    public MaterialRequisitionDTO getById(Long id) {
        MaterialRequisition req = requisitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("领料单不存在: " + id));
        return toDTO(req);
    }

    public MaterialRequisitionDTO getByReqNo(String reqNo) {
        MaterialRequisition req = requisitionRepository.findByReqNo(reqNo)
                .orElseThrow(() -> new IllegalArgumentException("领料单不存在: " + reqNo));
        return toDTO(req);
    }

    public Page<MaterialRequisitionDTO> page(int pageNum, int pageSize, Long orderId, Integer status) {
        long offset = (long) (pageNum - 1) * pageSize;
        long total = requisitionRepository.count(orderId, status);
        List<MaterialRequisition> list = requisitionRepository.findAll(offset, pageSize, orderId, status);
        List<MaterialRequisitionDTO> records = list.stream().map(this::toDTO).collect(Collectors.toList());
        Page<MaterialRequisitionDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        MaterialRequisition req = requisitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("领料单不存在: " + id));
        if (req.getStatus() != MaterialRequisition.STATUS_DRAFT) {
            throw new IllegalArgumentException("只有草稿状态可提交");
        }
        req.setStatus(MaterialRequisition.STATUS_SUBMITTED);
        req.setUpdateTime(LocalDateTime.now());
        requisitionRepository.save(req);
    }

    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id, RequisitionCompleteRequest request) {
        MaterialRequisition req = requisitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("领料单不存在: " + id));
        if (req.getStatus() == MaterialRequisition.STATUS_DONE) {
            throw new IllegalArgumentException("领料单已完成");
        }
        if (req.getStatus() == MaterialRequisition.STATUS_CANCELLED) {
            throw new IllegalArgumentException("已取消的领料单不能完成");
        }
        List<MaterialRequisitionDetail> details = requisitionRepository.findDetailsByReqId(id);
        if (details.isEmpty()) {
            throw new IllegalArgumentException("领料单无明细");
        }
        if (request != null && request.getDetails() != null && !request.getDetails().isEmpty()) {
            Map<Long, Integer> actualMap = request.getDetails().stream()
                    .collect(Collectors.toMap(RequisitionCompleteRequest.DetailActualQuantity::getDetailId,
                            RequisitionCompleteRequest.DetailActualQuantity::getActualQuantity));
            for (MaterialRequisitionDetail d : details) {
                Integer actual = actualMap.get(d.getId());
                if (actual != null && actual >= 0) {
                    if (actual > (d.getQuantity() != null ? d.getQuantity() : 0)) {
                        throw new IllegalArgumentException("物料ID " + d.getMaterialId() + " 实发数量不能大于申请数量");
                    }
                    d.setActualQuantity(actual);
                    requisitionRepository.updateDetail(d);
                }
            }
        }
        // 重新加载更新后的明细，执行库存变动
        details = requisitionRepository.findDetailsByReqId(id);
        for (MaterialRequisitionDetail d : details) {
            int actualQty = d.getActualQuantity() != null ? d.getActualQuantity() : 0;
            if (actualQty <= 0) continue;
            if (req.getReqType() == MaterialRequisition.REQ_TYPE_REQUISITION) {
                int available = inventoryApplicationService.getTotalQuantityByMaterialId(d.getMaterialId());
                if (available < actualQty) {
                    throw new IllegalArgumentException("物料ID " + d.getMaterialId() + " 库存不足，可用: " + available + ", 需: " + actualQty);
                }
            }
        }
        for (MaterialRequisitionDetail d : details) {
            int actualQty = d.getActualQuantity() != null ? d.getActualQuantity() : 0;
            if (actualQty <= 0) continue;
            String refNo = req.getReqNo();
            if (req.getReqType() == MaterialRequisition.REQ_TYPE_REQUISITION) {
                inventoryApplicationService.deductForRequisition(d.getMaterialId(), actualQty, refNo);
            } else if (req.getReqType() == MaterialRequisition.REQ_TYPE_RETURN) {
                inventoryApplicationService.addForReturn(d.getMaterialId(), actualQty, refNo);
            }
        }
        req.setStatus(MaterialRequisition.STATUS_DONE);
        req.setUpdateTime(LocalDateTime.now());
        requisitionRepository.save(req);
    }

    private MaterialRequisitionDTO toDTO(MaterialRequisition req) {
        MaterialRequisitionDTO dto = new MaterialRequisitionDTO();
        dto.setId(req.getId());
        dto.setReqNo(req.getReqNo());
        dto.setOrderId(req.getOrderId());
        dto.setReqType(req.getReqType());
        dto.setStatus(req.getStatus());
        dto.setCreateTime(req.getCreateTime());
        dto.setUpdateTime(req.getUpdateTime());
        List<MaterialRequisitionDetail> details = requisitionRepository.findDetailsByReqId(req.getId());
        dto.setDetails(details.stream().map(d -> toDetailDTO(d)).collect(Collectors.toList()));
        return dto;
    }

    private MaterialRequisitionDetailDTO toDetailDTO(MaterialRequisitionDetail d) {
        MaterialRequisitionDetailDTO dto = new MaterialRequisitionDetailDTO();
        dto.setId(d.getId());
        dto.setReqId(d.getReqId());
        dto.setMaterialId(d.getMaterialId());
        dto.setQuantity(d.getQuantity());
        dto.setActualQuantity(d.getActualQuantity() != null ? d.getActualQuantity() : 0);
        materialRepository.findById(d.getMaterialId()).ifPresent(m -> {
            dto.setMaterialCode(m.getMaterialCode());
            dto.setMaterialName(m.getMaterialName());
        });
        return dto;
    }
}
