package com.ly.factmesh.wms.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inventory_transaction")
public class InventoryTransactionEntity {

    @TableId("id")
    private Long id;
    @TableField("material_id")
    private Long materialId;
    @TableField("transaction_type")
    private Integer transactionType;
    @TableField("quantity")
    private Integer quantity;
    @TableField("warehouse")
    private String warehouse;
    @TableField("location")
    private String location;
    @TableField("transaction_time")
    private LocalDateTime transactionTime;
    @TableField("operator")
    private String operator;
    @TableField("reference_no")
    private String referenceNo;
}
