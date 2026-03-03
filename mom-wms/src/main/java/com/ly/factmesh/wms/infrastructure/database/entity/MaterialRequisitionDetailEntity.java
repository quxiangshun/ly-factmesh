package com.ly.factmesh.wms.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("material_requisition_detail")
public class MaterialRequisitionDetailEntity {

    @TableId("id")
    private Long id;
    @TableField("req_id")
    private Long reqId;
    @TableField("material_id")
    private Long materialId;
    @TableField("batch_no")
    private String batchNo;
    @TableField("quantity")
    private Integer quantity;
    @TableField("actual_quantity")
    private Integer actualQuantity;
}
