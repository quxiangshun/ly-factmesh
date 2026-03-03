package com.ly.factmesh.mes.domain.entity;

import com.ly.factmesh.common.enums.WorkOrderStatusEnum;

import java.time.LocalDateTime;

/**
 * 工单领域实体
 * 状态码参见 {@link WorkOrderStatusEnum}
 *
 * @author LY-FactMesh
 */
public class WorkOrder {

    /** 主键 */
    private Long id;
    /** 工单编码，唯一 */
    private String orderCode;
    /** 产品编码 */
    private String productCode;
    /** 产品名称 */
    private String productName;
    /** 计划数量 */
    private Integer planQuantity;
    /** 实际完成数量（报工累加） */
    private Integer actualQuantity;
    /** 状态码，参见 WorkOrderStatusEnum */
    private Integer status;
    /** 产线 ID，可选 */
    private Long lineId;
    /** 开始生产时间 */
    private LocalDateTime startTime;
    /** 完工时间 */
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

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
    public Long getLineId() { return lineId; }
    public void setLineId(Long lineId) { this.lineId = lineId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
