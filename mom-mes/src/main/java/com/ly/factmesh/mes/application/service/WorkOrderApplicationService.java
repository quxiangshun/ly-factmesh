package com.ly.factmesh.mes.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.feign.dto.QmsInspectionTaskRequest;
import com.ly.factmesh.common.feign.dto.RequisitionCreateRequest;
import com.ly.factmesh.common.feign.QmsFeignClient;
import com.ly.factmesh.common.feign.WmsFeignClient;
import com.ly.factmesh.mes.application.dto.WorkOrderCreateRequest;
import com.ly.factmesh.mes.application.dto.WorkOrderDTO;
import com.ly.factmesh.mes.application.dto.WorkOrderStatsDTO;
import com.ly.factmesh.mes.application.dto.WorkOrderSummaryDTO;
import com.ly.factmesh.common.enums.WorkOrderStatusEnum;
import com.ly.factmesh.mes.domain.entity.WorkOrder;
import com.ly.factmesh.mes.domain.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工单应用服务（工单发布触发 WMS 领料，工单完成触发 QMS 质检）
 *
 * @author LY-FactMesh
 */
@Service
@RequiredArgsConstructor
public class WorkOrderApplicationService {

    private final WorkOrderRepository workOrderRepository;
    private final WmsFeignClient wmsFeignClient;
    private final QmsFeignClient qmsFeignClient;

    @Transactional(rollbackFor = Exception.class)
    public WorkOrderDTO create(WorkOrderCreateRequest request) {
        if (workOrderRepository.findByOrderCode(request.getOrderCode()).isPresent()) {
            throw new IllegalArgumentException("工单编码已存在: " + request.getOrderCode());
        }
        WorkOrder wo = new WorkOrder();
        wo.setOrderCode(request.getOrderCode());
        wo.setProductCode(request.getProductCode());
        wo.setProductName(request.getProductName());
        wo.setPlanQuantity(request.getPlanQuantity());
        wo.setActualQuantity(0);
        wo.setStatus(WorkOrderStatusEnum.DRAFT.getCode());
        wo.setLineId(request.getLineId());
        wo.setCreateTime(LocalDateTime.now());
        wo.setUpdateTime(LocalDateTime.now());
        WorkOrder saved = workOrderRepository.save(wo);
        return toDTO(saved);
    }

    public WorkOrderDTO getById(Long id) {
        return workOrderRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + id));
    }

    public Page<WorkOrderDTO> page(int pageNum, int pageSize) {
        long total = workOrderRepository.count();
        long offset = (long) (pageNum - 1) * pageSize;
        List<WorkOrder> list = workOrderRepository.findAll(offset, pageSize);
        List<WorkOrderDTO> records = list.stream().map(this::toDTO).collect(Collectors.toList());
        Page<WorkOrderDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    public WorkOrderDTO release(Long id) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + id));
        if (wo.getStatus() != null && wo.getStatus() != WorkOrderStatusEnum.DRAFT.getCode()) {
            throw new IllegalStateException("只有草稿状态工单可下发");
        }
        wo.setStatus(WorkOrderStatusEnum.RELEASED.getCode());
        wo.setUpdateTime(LocalDateTime.now());
        WorkOrder saved = workOrderRepository.save(wo);
        // 跨域：工单发布触发 WMS 领料单
        RequisitionCreateRequest req = new RequisitionCreateRequest();
        req.setWorkOrderId(saved.getId());
        req.setWorkOrderNo(saved.getOrderCode());
        req.setMaterialCode(saved.getProductCode());
        req.setMaterialName(saved.getProductName());
        req.setQuantity(saved.getPlanQuantity());
        wmsFeignClient.createRequisition(req);
        return toDTO(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public WorkOrderDTO start(Long id) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + id));
        if (wo.getStatus() == null || wo.getStatus() != WorkOrderStatusEnum.RELEASED.getCode()) {
            throw new IllegalStateException("只有已下发工单可开始");
        }
        wo.setStatus(WorkOrderStatusEnum.IN_PROGRESS.getCode());
        wo.setStartTime(LocalDateTime.now());
        wo.setUpdateTime(LocalDateTime.now());
        return toDTO(workOrderRepository.save(wo));
    }

    @Transactional(rollbackFor = Exception.class)
    public WorkOrderDTO pause(Long id) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + id));
        if (wo.getStatus() == null || wo.getStatus() != WorkOrderStatusEnum.IN_PROGRESS.getCode()) {
            throw new IllegalStateException("只有进行中工单可暂停");
        }
        wo.setStatus(WorkOrderStatusEnum.PAUSED.getCode());
        wo.setUpdateTime(LocalDateTime.now());
        return toDTO(workOrderRepository.save(wo));
    }

    @Transactional(rollbackFor = Exception.class)
    public WorkOrderDTO resume(Long id) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + id));
        if (wo.getStatus() == null || wo.getStatus() != WorkOrderStatusEnum.PAUSED.getCode()) {
            throw new IllegalStateException("只有暂停工单可恢复");
        }
        wo.setStatus(WorkOrderStatusEnum.IN_PROGRESS.getCode());
        wo.setUpdateTime(LocalDateTime.now());
        return toDTO(workOrderRepository.save(wo));
    }

    @Transactional(rollbackFor = Exception.class)
    public WorkOrderDTO complete(Long id, Integer actualQuantity) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + id));
        if (wo.getStatus() == null || (wo.getStatus() != WorkOrderStatusEnum.IN_PROGRESS.getCode() && wo.getStatus() != WorkOrderStatusEnum.PAUSED.getCode())) {
            throw new IllegalStateException("只有进行中或暂停工单可完成");
        }
        wo.setStatus(WorkOrderStatusEnum.COMPLETED.getCode());
        wo.setActualQuantity(actualQuantity != null ? actualQuantity : wo.getActualQuantity());
        wo.setEndTime(LocalDateTime.now());
        wo.setUpdateTime(LocalDateTime.now());
        WorkOrder saved = workOrderRepository.save(wo);
        // 跨域：工单完成触发 QMS 质检任务
        QmsInspectionTaskRequest qmsReq = new QmsInspectionTaskRequest();
        qmsReq.setTaskCode("IT-" + saved.getId() + "-" + System.currentTimeMillis());
        qmsReq.setOrderId(saved.getId());
        qmsReq.setOrderCode(saved.getOrderCode());
        qmsReq.setProductCode(saved.getProductCode());
        qmsReq.setInspectionType(0);
        qmsFeignClient.createInspectionTask(qmsReq);
        return toDTO(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        workOrderRepository.deleteById(id);
    }

    public WorkOrderStatsDTO getStats() {
        WorkOrderStatsDTO stats = new WorkOrderStatsDTO();
        stats.setTotal(workOrderRepository.count());
        stats.setDraftCount(workOrderRepository.countByStatus(WorkOrderStatusEnum.DRAFT.getCode()));
        stats.setReleasedCount(workOrderRepository.countByStatus(WorkOrderStatusEnum.RELEASED.getCode()));
        stats.setInProgressCount(workOrderRepository.countByStatus(WorkOrderStatusEnum.IN_PROGRESS.getCode()));
        stats.setPausedCount(workOrderRepository.countByStatus(WorkOrderStatusEnum.PAUSED.getCode()));
        stats.setCompletedCount(workOrderRepository.countByStatus(WorkOrderStatusEnum.COMPLETED.getCode()));
        return stats;
    }

    /**
     * 生产汇总（简易生产报表，按日统计）
     */
    public WorkOrderSummaryDTO getSummary(LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        WorkOrderSummaryDTO dto = new WorkOrderSummaryDTO();
        dto.setDate(target);
        dto.setCompletedCount(workOrderRepository.countCompletedOnDate(target));
        dto.setCompletedQuantity(workOrderRepository.sumActualQuantityCompletedOnDate(target));
        dto.setInProgressCount(workOrderRepository.countByStatus(WorkOrderStatusEnum.IN_PROGRESS.getCode()));
        dto.setPausedCount(workOrderRepository.countByStatus(WorkOrderStatusEnum.PAUSED.getCode()));
        return dto;
    }

    private WorkOrderDTO toDTO(WorkOrder wo) {
        WorkOrderDTO dto = new WorkOrderDTO();
        dto.setId(wo.getId());
        dto.setOrderCode(wo.getOrderCode());
        dto.setProductCode(wo.getProductCode());
        dto.setProductName(wo.getProductName());
        dto.setPlanQuantity(wo.getPlanQuantity());
        dto.setActualQuantity(wo.getActualQuantity());
        dto.setStatus(wo.getStatus());
        dto.setLineId(wo.getLineId());
        dto.setStartTime(wo.getStartTime());
        dto.setEndTime(wo.getEndTime());
        dto.setCreateTime(wo.getCreateTime());
        dto.setUpdateTime(wo.getUpdateTime());
        return dto;
    }
}
