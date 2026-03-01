package com.ly.factmesh.qms.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.qms.application.dto.InspectionTaskCreateRequest;
import com.ly.factmesh.qms.application.dto.InspectionTaskDTO;
import com.ly.factmesh.qms.domain.entity.InspectionTask;
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

    @Transactional(rollbackFor = Exception.class)
    public InspectionTaskDTO create(InspectionTaskCreateRequest request) {
        if (inspectionTaskRepository.findByTaskCode(request.getTaskCode()).isPresent()) {
            throw new IllegalArgumentException("任务编码已存在: " + request.getTaskCode());
        }
        InspectionTask t = new InspectionTask();
        t.setTaskCode(request.getTaskCode());
        t.setOrderId(request.getOrderId());
        t.setMaterialId(request.getMaterialId());
        t.setDeviceId(request.getDeviceId());
        t.setInspectionType(request.getInspectionType() != null ? request.getInspectionType() : 0);
        t.setStatus(InspectionTask.STATUS_DRAFT);
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

    public Page<InspectionTaskDTO> page(int pageNum, int pageSize) {
        long total = inspectionTaskRepository.count();
        long offset = (long) (pageNum - 1) * pageSize;
        List<InspectionTask> list = inspectionTaskRepository.findAll(offset, pageSize);
        List<InspectionTaskDTO> records = list.stream().map(this::toDTO).collect(Collectors.toList());
        Page<InspectionTaskDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    public InspectionTaskDTO complete(Long id) {
        InspectionTask t = inspectionTaskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("质检任务不存在: " + id));
        t.setStatus(InspectionTask.STATUS_COMPLETED);
        t.setInspectionTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        return toDTO(inspectionTaskRepository.save(t));
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
