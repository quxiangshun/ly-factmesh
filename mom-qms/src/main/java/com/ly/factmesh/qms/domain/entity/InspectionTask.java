package com.ly.factmesh.qms.domain.entity;

import java.time.LocalDateTime;

/**
 * 质检任务领域实体
 *
 * @author LY-FactMesh
 */
public class InspectionTask {

    private Long id;
    private String taskCode;
    private Long orderId;
    private String orderCode;
    private Long materialId;
    private String productCode;
    private Long deviceId;
    private Integer inspectionType;
    private LocalDateTime inspectionTime;
    private Integer status;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_IN_PROGRESS = 1;
    public static final int STATUS_COMPLETED = 2;
    public static final int STATUS_CLOSED = 3;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String taskCode) { this.taskCode = taskCode; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public Long getMaterialId() { return materialId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    public Integer getInspectionType() { return inspectionType; }
    public void setInspectionType(Integer inspectionType) { this.inspectionType = inspectionType; }
    public LocalDateTime getInspectionTime() { return inspectionTime; }
    public void setInspectionTime(LocalDateTime inspectionTime) { this.inspectionTime = inspectionTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
