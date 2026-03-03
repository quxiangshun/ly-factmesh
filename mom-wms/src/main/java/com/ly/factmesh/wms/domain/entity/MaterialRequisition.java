package com.ly.factmesh.wms.domain.entity;

import com.ly.factmesh.common.enums.RequisitionStatusEnum;
import com.ly.factmesh.common.enums.RequisitionTypeEnum;

import java.time.LocalDateTime;

/**
 * 领料单领域实体
 * 状态码参见 {@link RequisitionStatusEnum}，类型参见 {@link RequisitionTypeEnum}
 *
 * @author LY-FactMesh
 */
public class MaterialRequisition {

    private Long id;
    private String reqNo;
    private Long orderId;
    private Integer reqType;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReqNo() { return reqNo; }
    public void setReqNo(String reqNo) { this.reqNo = reqNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Integer getReqType() { return reqType; }
    public void setReqType(Integer reqType) { this.reqType = reqType; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
