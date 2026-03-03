package com.ly.factmesh.wms.domain.entity;

import com.ly.factmesh.common.enums.InventoryTransactionTypeEnum;

import java.time.LocalDateTime;

/**
 * 出入库记录领域实体
 * 事务类型参见 {@link InventoryTransactionTypeEnum}
 *
 * @author LY-FactMesh
 */
public class InventoryTransaction {

    private Long id;
    private Long materialId;
    private String batchNo;
    private Integer transactionType;
    private Integer quantity;
    private String warehouse;
    private String location;
    private Long orderId;
    private Long reqId;
    private LocalDateTime transactionTime;
    private String operator;
    private String referenceNo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Integer getTransactionType() { return transactionType; }
    public void setTransactionType(Integer transactionType) { this.transactionType = transactionType; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getReqId() { return reqId; }
    public void setReqId(Long reqId) { this.reqId = reqId; }
    public LocalDateTime getTransactionTime() { return transactionTime; }
    public void setTransactionTime(LocalDateTime transactionTime) { this.transactionTime = transactionTime; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
}
