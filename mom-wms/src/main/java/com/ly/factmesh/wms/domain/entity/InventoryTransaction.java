package com.ly.factmesh.wms.domain.entity;

import java.time.LocalDateTime;

/**
 * 出入库记录领域实体
 *
 * @author LY-FactMesh
 */
public class InventoryTransaction {

    private Long id;
    private Long materialId;
    private Integer transactionType;
    private Integer quantity;
    private String warehouse;
    private String location;
    private LocalDateTime transactionTime;
    private String operator;
    private String referenceNo;

    /** 入库 */
    public static final int TYPE_INBOUND = 1;
    /** 出库 */
    public static final int TYPE_OUTBOUND = 2;
    /** 盘点调整 */
    public static final int TYPE_ADJUST = 3;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public Integer getTransactionType() { return transactionType; }
    public void setTransactionType(Integer transactionType) { this.transactionType = transactionType; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getTransactionTime() { return transactionTime; }
    public void setTransactionTime(LocalDateTime transactionTime) { this.transactionTime = transactionTime; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
}
