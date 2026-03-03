package com.ly.factmesh.qms.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inspection_result")
public class InspectionResultEntity {

    @TableId("id")
    private Long id;
    @TableField("task_id")
    private Long taskId;
    @TableField("inspection_item")
    private String inspectionItem;
    @TableField("standard_value")
    private String standardValue;
    @TableField("actual_value")
    private String actualValue;
    @TableField("judgment")
    private Integer judgment;
    @TableField("inspector")
    private String inspector;
    @TableField("inspection_time")
    private LocalDateTime inspectionTime;
    @TableField("create_time")
    private LocalDateTime createTime;
}
