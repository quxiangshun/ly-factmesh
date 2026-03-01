package com.ly.factmesh.mes.domain.entity;

import java.time.LocalDateTime;

/**
 * 工单领域实体
 *
 * @author LY-FactMesh
 */
public class WorkOrder {

    private Long id;
    private String orderCode;
    private String productCode;
    private String productName;
    private Integer planQuantity;
    private Integer actualQuantity;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_RELEASED = 1;
    public static final int STATUS_IN_PROGRESS = 2;
    public static final int STATUS_COMPLETED = 3;
    public static final int STATUS_CLOSED = 4;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getPlanQuantity() { return planQuantity; }
    public void setPlanQuantity(Integer planQuantity) { this.planQuantity = planQuantity; }
    public Integer getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(Integer actualQuantity) { this.actualQuantity = actualQuantity; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
