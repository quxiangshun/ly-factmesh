package com.ly.factmesh.ops.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运维审计日志实体（跨服务审计记录）
 *
 * @author LY-FactMesh
 */
@Data
@TableName("audit_log")
public class AuditLogEntity {

    @TableId("id")
    private Long id;
    @TableField("service_name")
    private String serviceName;
    @TableField("user_id")
    private Long userId;
    @TableField("username")
    private String username;
    @TableField("operation_type")
    private String operationType;
    @TableField("operation_content")
    private String operationContent;
    @TableField("operation_result")
    private Integer operationResult;
    @TableField("operation_time")
    private LocalDateTime operationTime;
    @TableField("client_ip")
    private String clientIp;
    @TableField("request_params")
    private String requestParams;
}
