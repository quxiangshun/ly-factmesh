package com.ly.factmesh.ops.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统事件实体
 *
 * @author LY-FactMesh
 */
@Data
@TableName("system_event")
public class SystemEventEntity {

    @TableId("id")
    private Long id;
    @TableField("event_type")
    private String eventType;
    @TableField("event_level")
    private Integer eventLevel;
    @TableField("event_content")
    private String eventContent;
    @TableField("related_service")
    private String relatedService;
    @TableField("related_id")
    private String relatedId;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("processed")
    private Integer processed;
    @TableField("process_time")
    private LocalDateTime processTime;
}
