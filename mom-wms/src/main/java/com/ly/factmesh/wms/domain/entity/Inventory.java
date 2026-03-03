package com.ly.factmesh.wms.domain.entity;

import java.time.LocalDateTime;

/**
 * 库存领域实体
 *
 * @author LY-FactMesh
 */
public class Inventory {

    /** 主键 */
    private Long id;
    /** 物料 ID */
    private Long materialId;
    /** 批次号，可选 */
    private String batchNo;
    /** 仓库 */
    private String warehouse;
    /** 库位 */
    private String location;
    /** 当前数量 */
    private Integer quantity;
    /** 安全库存，低于此值时预警 */
    private Integer safeStock;
    private LocalDateTime lastUpdateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getSafeStock() { return safeStock; }
    public void setSafeStock(Integer safeStock) { this.safeStock = safeStock; }
    public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
}
