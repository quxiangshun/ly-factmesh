package com.ly.factmesh.qms.domain.entity;

import java.time.LocalDateTime;

/**
 * 质量追溯领域实体（关联工单/设备/物料）
 *
 * @author LY-FactMesh
 */
public class QualityTraceability {

    private Long id;
    private String productCode;
    private String batchNo;
    private String materialBatch;
    private String productionOrder;
    private Long inspectionRecordId;
    private Long nonConformingId;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getMaterialBatch() { return materialBatch; }
    public void setMaterialBatch(String materialBatch) { this.materialBatch = materialBatch; }
    public String getProductionOrder() { return productionOrder; }
    public void setProductionOrder(String productionOrder) { this.productionOrder = productionOrder; }
    public Long getInspectionRecordId() { return inspectionRecordId; }
    public void setInspectionRecordId(Long inspectionRecordId) { this.inspectionRecordId = inspectionRecordId; }
    public Long getNonConformingId() { return nonConformingId; }
    public void setNonConformingId(Long nonConformingId) { this.nonConformingId = nonConformingId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
