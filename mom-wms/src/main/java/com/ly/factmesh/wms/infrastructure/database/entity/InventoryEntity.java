package com.ly.factmesh.wms.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inventory")
public class InventoryEntity {

    @TableId("id")
    private Long id;
    @TableField("material_id")
    private Long materialId;
    @TableField("warehouse")
    private String warehouse;
    @TableField("location")
    private String location;
    @TableField("quantity")
    private Integer quantity;
    @TableField("safe_stock")
    private Integer safeStock;
    @TableField("last_update_time")
    private LocalDateTime lastUpdateTime;
}
