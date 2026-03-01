package com.ly.factmesh.wms.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("material_info")
public class MaterialEntity {

    @TableId("id")
    private Long id;
    @TableField("material_code")
    private String materialCode;
    @TableField("material_name")
    private String materialName;
    @TableField("material_type")
    private String materialType;
    @TableField("specification")
    private String specification;
    @TableField("unit")
    private String unit;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
