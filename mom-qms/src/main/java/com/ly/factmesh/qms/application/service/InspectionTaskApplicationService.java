package com.ly.factmesh.qms.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.qms.application.dto.*;
import com.ly.factmesh.qms.domain.entity.InspectionResult;
import com.ly.factmesh.common.enums.InspectionJudgmentEnum;
import com.ly.factmesh.common.enums.InspectionTaskStatusEnum;
import com.ly.factmesh.qms.domain.entity.InspectionTask;
import com.ly.factmesh.qms.domain.repository.InspectionResultRepository;
import com.ly.factmesh.qms.domain.repository.InspectionTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspectionTaskApplicationService {

    private final InspectionTaskRepository inspectionTaskRepository;
    private final InspectionResultRepository inspectionResultRepository;

    @Transactional(rollbackFor = Exception.class)
    public InspectionTaskDTO create(InspectionTaskCreateRequest request) {
        if (inspectionTaskRepository.findByTaskCode(request.getTaskCode()).isPresent()) {
            throw new IllegalArgumentException("任务编码已存在: " + request.getTaskCode());
        }
        InspectionTask t = new InspectionTask();
        t.setTaskCode(request.getTaskCode());
        t.setOrderId(request.getOrderId());
        t.setOrderCode(request.getOrderCode());
        t.setProductCode(request.getProductCode());
        t.setMaterialId(request.getMaterialId());
        t.setDeviceId(request.getDeviceId());
        t.setInspectionType(request.getInspectionType() != null ? request.getInspectionType() : 0);
        t.setStatus(InspectionTaskStatusEnum.DRAFT.getCode());
        t.setCreateTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        InspectionTask saved = inspectionTaskRepository.save(t);
        return toDTO(saved);
    }

    public InspectionTaskDTO getById(Long id) {
        return inspectionTaskRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("质检任务不存在: " + id));
    }

    public Page<InspectionTaskDTO> page(int pageNum, int pageSize, Integer status) {
        long total = inspectionTaskRepository.countByStatus(status);
        long offset = (long) (pageNum - 1) * pageSize;
        List<InspectionTask> list = inspectionTaskRepository.findAll(offset, pageSize, status);
        List<InspectionTaskDTO> records = list.stream().map(this::toDTO).collect(Collectors.toList());
        Page<InspectionTaskDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    public InspectionTaskDTO start(Long id) {
        InspectionTask t = inspectionTaskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("质检任务不存在: " + id));
        if (t.getStatus() == null || t.getStatus() != InspectionTaskStatusEnum.DRAFT.getCode()) {
            throw new IllegalArgumentException("只有待检状态的任务可以开始");
        }
        t.setStatus(InspectionTaskStatusEnum.IN_PROGRESS.getCode());
        t.setUpdateTime(LocalDateTime.now());
        return toDTO(inspectionTaskRepository.save(t));
    }

    @Transactional(rollbackFor = Exception.class)
    public InspectionTaskDTO complete(Long id, InspectionTaskCompleteRequest request) {
        InspectionTask t = inspectionTaskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("质检任务不存在: " + id));
        if (t.getStatus() == null || t.getStatus() != InspectionTaskStatusEnum.IN_PROGRESS.getCode()) {
            throw new IllegalArgumentException("只有检验中状态的任务可以完成");
        }
        List<InspectionResult> results = inspectionResultRepository.findByTaskId(id);
        long failCount = results.stream().filter(r -> r.getJudgment() != null && r.getJudgment() == InspectionJudgmentEnum.FAIL.getCode()).count();
        if (failCount > 0 && (request == null || !Boolean.TRUE.equals(request.getForceComplete()))) {
            throw new IllegalArgumentException("存在 " + failCount + " 项不合格，请先创建不合格品或选择强制完成");
        }
        if (request != null && request.getOperator() != null && !request.getOperator().isBlank()) {
            t.setOperator(request.getOperator());
        }
        t.setStatus(InspectionTaskStatusEnum.COMPLETED.getCode());
        t.setInspectionTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        return toDTO(inspectionTaskRepository.save(t));
    }

    public InspectionTaskStatsDTO getStats() {
        InspectionTaskStatsDTO stats = new InspectionTaskStatsDTO();
        stats.setTotal(inspectionTaskRepository.countByStatus(null));
        stats.setDraftCount(inspectionTaskRepository.countByStatus(InspectionTaskStatusEnum.DRAFT.getCode()));
        stats.setInProgressCount(inspectionTaskRepository.countByStatus(InspectionTaskStatusEnum.IN_PROGRESS.getCode()));
        stats.setCompletedCount(inspectionTaskRepository.countByStatus(InspectionTaskStatusEnum.COMPLETED.getCode()));
        return stats;
    }

    public InspectionTaskNcrContextDTO getNcrContext(Long taskId) {
        InspectionTask t = inspectionTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("质检任务不存在: " + taskId));
        InspectionTaskNcrContextDTO ctx = new InspectionTaskNcrContextDTO();
        ctx.setTaskId(t.getId());
        ctx.setTaskCode(t.getTaskCode());
        ctx.setOrderId(t.getOrderId());
        ctx.setMaterialId(t.getMaterialId());
        ctx.setSuggestedProductCode(t.getMaterialId() != null ? "M" + t.getMaterialId() : null);
        return ctx;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        inspectionTaskRepository.deleteById(id);
    }

    private InspectionTaskDTO toDTO(InspectionTask t) {
        InspectionTaskDTO dto = new InspectionTaskDTO();
        dto.setId(t.getId());
        dto.setTaskCode(t.getTaskCode());
        dto.setOrderId(t.getOrderId());
        dto.setOrderCode(t.getOrderCode());
        dto.setProductCode(t.getProductCode());
        dto.setMaterialId(t.getMaterialId());
        dto.setDeviceId(t.getDeviceId());
        dto.setInspectionType(t.getInspectionType());
        dto.setInspectionTime(t.getInspectionTime());
        dto.setStatus(t.getStatus());
        dto.setOperator(t.getOperator());
        dto.setCreateTime(t.getCreateTime());
        dto.setUpdateTime(t.getUpdateTime());
        return dto;
    }
}
