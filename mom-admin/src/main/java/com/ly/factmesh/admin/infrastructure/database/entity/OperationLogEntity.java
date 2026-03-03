package com.ly.factmesh.admin.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志数据库实体
 *
 * @author LY-FactMesh
 */
@Data
@TableName("sys_operation_log")
public class OperationLogEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("username")
    private String username;
    @TableField("module")
    private String module;
    @TableField("operation")
    private String operation;
    @TableField("method")
    private String method;
    @TableField("url")
    private String url;
    @TableField("params")
    private String params;
    @TableField("ip")
    private String ip;
    @TableField("status")
    private Integer status;
    @TableField("error_msg")
    private String errorMsg;
    @TableField("duration")
    private Long duration;
    @TableField("create_time")
    private LocalDateTime createTime;
}
