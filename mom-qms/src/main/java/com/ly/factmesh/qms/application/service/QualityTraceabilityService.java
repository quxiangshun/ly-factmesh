package com.ly.factmesh.qms.application.service;

import com.ly.factmesh.qms.application.dto.QualityTraceabilityDTO;
import com.ly.factmesh.qms.domain.entity.QualityTraceability;
import com.ly.factmesh.qms.domain.repository.QualityTraceabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 质量追溯应用服务（关联工单/设备/物料）
 *
 * @author LY-FactMesh
 */
@Service
@RequiredArgsConstructor
public class QualityTraceabilityService {

    private final QualityTraceabilityRepository qualityTraceabilityRepository;

    public List<QualityTraceabilityDTO> trace(String productCode, String batchNo, String productionOrder) {
        return qualityTraceabilityRepository.findTrace(productCode, batchNo, productionOrder)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void createTraceFromInspection(Long inspectionResultId, String productCode, String batchNo, String productionOrder) {
        if (productCode == null || productCode.isBlank()) return;
        QualityTraceability t = new QualityTraceability();
        t.setProductCode(productCode);
        t.setBatchNo(batchNo);
        t.setProductionOrder(productionOrder);
        t.setInspectionRecordId(inspectionResultId);
        qualityTraceabilityRepository.save(t);
    }

    public void createTraceFromNcr(Long nonConformingId, String productCode, String batchNo, String productionOrder) {
        if (productCode == null || productCode.isBlank()) return;
        QualityTraceability t = new QualityTraceability();
        t.setProductCode(productCode);
        t.setBatchNo(batchNo);
        t.setProductionOrder(productionOrder);
        t.setNonConformingId(nonConformingId);
        qualityTraceabilityRepository.save(t);
    }

    private QualityTraceabilityDTO toDTO(QualityTraceability t) {
        return QualityTraceabilityDTO.builder()
                .id(t.getId())
                .productCode(t.getProductCode())
                .batchNo(t.getBatchNo())
                .materialBatch(t.getMaterialBatch())
                .productionOrder(t.getProductionOrder())
                .inspectionRecordId(t.getInspectionRecordId())
                .nonConformingId(t.getNonConformingId())
                .createTime(t.getCreateTime())
                .build();
    }
}
