package com.ly.factmesh.mes.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产线数据库实体
 *
 * @author LY-FactMesh
 */
@Data
@TableName("production_line")
public class ProductionLineEntity {

    @TableId("id")
    private Long id;
    @TableField("line_code")
    private String lineCode;
    @TableField("line_name")
    private String lineName;
    @TableField("description")
    private String description;
    @TableField("sequence")
    private Integer sequence;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
