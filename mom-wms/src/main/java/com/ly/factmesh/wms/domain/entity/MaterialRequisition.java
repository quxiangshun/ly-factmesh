package com.ly.factmesh.wms.domain.entity;

import java.time.LocalDateTime;

/**
 * 领料单领域实体
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

    public static final int REQ_TYPE_REQUISITION = 1;
    public static final int REQ_TYPE_RETURN = 2;
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_SUBMITTED = 1;
    public static final int STATUS_DONE = 2;

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
