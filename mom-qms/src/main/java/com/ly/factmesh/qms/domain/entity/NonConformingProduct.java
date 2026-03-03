package com.ly.factmesh.qms.domain.entity;

import java.time.LocalDateTime;

/**
 * 不合格品领域实体（NCR - Non-Conforming Report）
 *
 * @author LY-FactMesh
 */
public class NonConformingProduct {

    private Long id;
    private String ncrNo;
    private String productCode;
    private String batchNo;
    private Integer quantity;
    private String reason;
    private Integer disposalMethod;
    private Integer disposalResult;
    private Long taskId;
    private LocalDateTime createTime;
    private LocalDateTime disposeTime;
    private String remark;

    /** 处置方式：0待处置 1返工 2报废 3让步接收 4退货 */
    public static final int DISPOSAL_PENDING = 0;
    public static final int DISPOSAL_REWORK = 1;
    public static final int DISPOSAL_SCRAP = 2;
    public static final int DISPOSAL_CONCESSION = 3;
    public static final int DISPOSAL_RETURN = 4;

    /** 处置结果：0待处理 1已处理 */
    public static final int RESULT_PENDING = 0;
    public static final int RESULT_DONE = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNcrNo() { return ncrNo; }
    public void setNcrNo(String ncrNo) { this.ncrNo = ncrNo; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getDisposalMethod() { return disposalMethod; }
    public void setDisposalMethod(Integer disposalMethod) { this.disposalMethod = disposalMethod; }
    public Integer getDisposalResult() { return disposalResult; }
    public void setDisposalResult(Integer disposalResult) { this.disposalResult = disposalResult; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getDisposeTime() { return disposeTime; }
    public void setDisposeTime(LocalDateTime disposeTime) { this.disposeTime = disposeTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
