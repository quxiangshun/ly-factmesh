package com.ly.factmesh.mes.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工序数据库实体
 *
 * @author LY-FactMesh
 */
@Data
@TableName("process")
public class ProcessEntity {

    @TableId("id")
    private Long id;
    @TableField("process_code")
    private String processCode;
    @TableField("process_name")
    private String processName;
    @TableField("sequence")
    private Integer sequence;
    @TableField("work_center")
    private String workCenter;
    @TableField("create_time")
    private LocalDateTime createTime;
}
