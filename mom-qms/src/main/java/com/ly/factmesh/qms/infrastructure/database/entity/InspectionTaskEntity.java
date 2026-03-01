package com.ly.factmesh.qms.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inspection_task")
public class InspectionTaskEntity {

    @TableId("id")
    private Long id;
    @TableField("task_code")
    private String taskCode;
    @TableField("order_id")
    private Long orderId;
    @TableField("material_id")
    private Long materialId;
    @TableField("device_id")
    private Long deviceId;
    @TableField("inspection_type")
    private Integer inspectionType;
    @TableField("inspection_time")
    private LocalDateTime inspectionTime;
    @TableField("status")
    private Integer status;
    @TableField("operator")
    private String operator;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
