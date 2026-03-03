package com.ly.factmesh.ops.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("global_log")
public class GlobalLogEntity {

    @TableId("id")
    private Long id;
    @TableField("service_name")
    private String serviceName;
    @TableField("log_type")
    private Integer logType;
    @TableField("log_level")
    private String logLevel;
    @TableField("log_content")
    private String logContent;
    @TableField("request_id")
    private String requestId;
    @TableField("client_ip")
    private String clientIp;
    @TableField("create_time")
    private LocalDateTime createTime;
}
