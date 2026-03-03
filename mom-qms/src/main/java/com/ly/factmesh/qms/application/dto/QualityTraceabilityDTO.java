package com.ly.factmesh.qms.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 质量追溯 DTO
 *
 * @author LY-FactMesh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityTraceabilityDTO {

    private Long id;
    private String productCode;
    private String batchNo;
    private String materialBatch;
    private String productionOrder;
    private Long inspectionRecordId;
    private Long nonConformingId;
    private LocalDateTime createTime;
}
