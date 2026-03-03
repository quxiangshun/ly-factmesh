package com.ly.factmesh.qms.application.service;

import com.ly.factmesh.qms.application.dto.InspectionResultCreateRequest;
import com.ly.factmesh.qms.application.dto.InspectionResultDTO;
import com.ly.factmesh.common.enums.InspectionJudgmentEnum;
import com.ly.factmesh.qms.domain.entity.InspectionResult;
import com.ly.factmesh.qms.domain.repository.InspectionResultRepository;
import com.ly.factmesh.qms.domain.repository.InspectionTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspectionResultApplicationService {

    private final InspectionResultRepository inspectionResultRepository;
    private final InspectionTaskRepository inspectionTaskRepository;
    private final QualityTraceabilityService qualityTraceabilityService;

    @Transactional(rollbackFor = Exception.class)
    public InspectionResultDTO create(InspectionResultCreateRequest request) {
        var task = inspectionTaskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("质检任务不存在: " + request.getTaskId()));
        InspectionResult r = new InspectionResult();
        r.setTaskId(request.getTaskId());
        r.setInspectionItem(request.getInspectionItem());
        r.setStandardValue(request.getStandardValue());
        r.setActualValue(request.getActualValue());
        r.setJudgment(request.getJudgment() != null ? request.getJudgment() : InspectionJudgmentEnum.PASS.getCode());
        r.setInspector(request.getInspector());
        InspectionResult saved = inspectionResultRepository.save(r);
        if (saved.getJudgment() != null && saved.getJudgment() == InspectionJudgmentEnum.FAIL.getCode()) {
            String productCode = task.getProductCode();
            if (productCode == null && task.getMaterialId() != null) {
                productCode = "M" + task.getMaterialId();
            }
            if (productCode != null) {
                qualityTraceabilityService.createTraceFromInspection(saved.getId(), productCode, null, task.getOrderCode());
            }
        }
        return toDTO(saved);
    }

    public InspectionResultDTO getById(Long id) {
        return inspectionResultRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("质检结果不存在: " + id));
    }

    public List<InspectionResultDTO> listByTaskId(Long taskId) {
        return inspectionResultRepository.findByTaskId(taskId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        inspectionResultRepository.deleteById(id);
    }

    private InspectionResultDTO toDTO(InspectionResult r) {
        InspectionResultDTO dto = new InspectionResultDTO();
        dto.setId(r.getId());
        dto.setTaskId(r.getTaskId());
        dto.setInspectionItem(r.getInspectionItem());
        dto.setStandardValue(r.getStandardValue());
        dto.setActualValue(r.getActualValue());
        dto.setJudgment(r.getJudgment());
        dto.setInspector(r.getInspector());
        dto.setInspectionTime(r.getInspectionTime());
        dto.setCreateTime(r.getCreateTime());
        return dto;
    }
}
