package com.ly.factmesh.wms.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.feign.dto.RequisitionCreateRequest;
import com.ly.factmesh.wms.application.dto.MaterialRequisitionDetailDTO;
import com.ly.factmesh.wms.application.dto.MaterialRequisitionDTO;
import com.ly.factmesh.wms.application.dto.RequisitionCompleteRequest;
import com.ly.factmesh.wms.application.dto.RequisitionManualCreateRequest;
import com.ly.factmesh.wms.domain.entity.Material;
import com.ly.factmesh.common.enums.RequisitionStatusEnum;
import com.ly.factmesh.common.enums.RequisitionTypeEnum;
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
 * 领料单应用服务
 * <p>
 * 类型：领料（出库）、退料（入库）。来源：MES 工单下发触发 或 手动创建草稿。
 * 流程：草稿 -> 提交 -> 完成。完成时根据类型执行库存扣减或入库，并记录事务。
 * </p>
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
     * 由 MES 工单下发触发的领料单创建。物料不存在时按编码自动创建，领料单直接为已提交状态
     *
     * @param request 领料请求（工单 ID、物料、数量）
     * @return 领料单 ID
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
        req.setReqType(RequisitionTypeEnum.REQUISITION.getCode());
        req.setStatus(RequisitionStatusEnum.SUBMITTED.getCode());
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
     *
     * @param request 领料参数
     * @return 领料单 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createDraft(RequisitionManualCreateRequest request) {
        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new IllegalArgumentException("物料不存在: " + request.getMaterialId()));
        String reqNo = "REQ-M-" + System.currentTimeMillis();
        MaterialRequisition req = new MaterialRequisition();
        req.setReqNo(reqNo);
        req.setOrderId(request.getOrderId());
        req.setReqType(request.getReqType() != null ? request.getReqType() : RequisitionTypeEnum.REQUISITION.getCode());
        req.setStatus(RequisitionStatusEnum.DRAFT.getCode());
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

    /**
     * 取消领料单，已完成或已取消的不可再取消
     *
     * @param id 领料单 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        MaterialRequisition req = requisitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("领料单不存在: " + id));
        if (req.getStatus() != null && req.getStatus() == RequisitionStatusEnum.DONE.getCode()) {
            throw new IllegalArgumentException("已完成的领料单不能取消");
        }
        if (req.getStatus() != null && req.getStatus() == RequisitionStatusEnum.CANCELLED.getCode()) {
            throw new IllegalArgumentException("领料单已取消");
        }
        req.setStatus(RequisitionStatusEnum.CANCELLED.getCode());
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

    /**
     * 提交领料单：草稿 -> 已提交，提交后仓库可安排发货
     *
     * @param id 领料单 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        MaterialRequisition req = requisitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("领料单不存在: " + id));
        if (req.getStatus() == null || req.getStatus() != RequisitionStatusEnum.DRAFT.getCode()) {
            throw new IllegalArgumentException("只有草稿状态可提交");
        }
        req.setStatus(RequisitionStatusEnum.SUBMITTED.getCode());
        req.setUpdateTime(LocalDateTime.now());
        requisitionRepository.save(req);
    }

    /**
     * 完成领料：录入实发数量，校验库存（领料需库存充足），按类型扣减或增加库存
     *
     * @param id      领料单 ID
     * @param request 完成参数（各明细实发数量）
     */
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id, RequisitionCompleteRequest request) {
        MaterialRequisition req = requisitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("领料单不存在: " + id));
        if (req.getStatus() != null && req.getStatus() == RequisitionStatusEnum.DONE.getCode()) {
            throw new IllegalArgumentException("领料单已完成");
        }
        if (req.getStatus() != null && req.getStatus() == RequisitionStatusEnum.CANCELLED.getCode()) {
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
        // 先校验领料类单据库存是否充足，再执行库存变动
        details = requisitionRepository.findDetailsByReqId(id);
        for (MaterialRequisitionDetail d : details) {
            int actualQty = d.getActualQuantity() != null ? d.getActualQuantity() : 0;
            if (actualQty <= 0) continue;
            if (req.getReqType() != null && req.getReqType() == RequisitionTypeEnum.REQUISITION.getCode()) {
                int available = inventoryApplicationService.getTotalQuantityByMaterialId(d.getMaterialId());
                if (available < actualQty) {
                    throw new IllegalArgumentException("物料ID " + d.getMaterialId() + " 库存不足，可用: " + available + ", 需: " + actualQty);
                }
            }
        }
        // 领料：扣减库存；退料：增加库存。均记录事务用于追溯
        for (MaterialRequisitionDetail d : details) {
            int actualQty = d.getActualQuantity() != null ? d.getActualQuantity() : 0;
            if (actualQty <= 0) continue;
            String refNo = req.getReqNo();
            Long reqId = req.getId();
            Long orderId = req.getOrderId();
            if (req.getReqType() != null && req.getReqType() == RequisitionTypeEnum.REQUISITION.getCode()) {
                inventoryApplicationService.deductForRequisition(d.getMaterialId(), actualQty, refNo, reqId, orderId);
            } else if (req.getReqType() != null && req.getReqType() == RequisitionTypeEnum.RETURN.getCode()) {
                inventoryApplicationService.addForReturn(d.getMaterialId(), actualQty, refNo, reqId, orderId);
            }
        }
        req.setStatus(RequisitionStatusEnum.DONE.getCode());
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
        dto.setBatchNo(d.getBatchNo());
        dto.setQuantity(d.getQuantity());
        dto.setActualQuantity(d.getActualQuantity() != null ? d.getActualQuantity() : 0);
        materialRepository.findById(d.getMaterialId()).ifPresent(m -> {
            dto.setMaterialCode(m.getMaterialCode());
            dto.setMaterialName(m.getMaterialName());
        });
        return dto;
    }
}
