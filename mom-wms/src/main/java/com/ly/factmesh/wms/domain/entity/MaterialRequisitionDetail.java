package com.ly.factmesh.wms.domain.entity;

/**
 * 领料单明细领域实体
 *
 * @author LY-FactMesh
 */
public class MaterialRequisitionDetail {

    private Long id;
    private Long reqId;
    private Long materialId;
    private Integer quantity;
    private Integer actualQuantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReqId() { return reqId; }
    public void setReqId(Long reqId) { this.reqId = reqId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(Integer actualQuantity) { this.actualQuantity = actualQuantity; }
}
