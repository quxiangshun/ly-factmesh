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

    /** 主键 */
    private Long id;
    /** 领料单号，唯一 */
    private String reqNo;
    /** 关联工单 ID（MES 触发时有值） */
    private Long orderId;
    /** 类型：1-领料 2-退料，参见 RequisitionTypeEnum */
    private Integer reqType;
    /** 状态：0-草稿 1-已提交 2-已完成 3-已取消，参见 RequisitionStatusEnum */
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
