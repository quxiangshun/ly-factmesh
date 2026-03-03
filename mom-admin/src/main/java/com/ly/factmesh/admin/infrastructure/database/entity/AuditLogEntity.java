package com.ly.factmesh.admin.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志数据库实体
 *
 * @author LY-FactMesh
 */
@Data
@TableName("sys_audit_log")
public class AuditLogEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("table_name")
    private String tableName;
    @TableField("record_id")
    private String recordId;
    @TableField("operation_type")
    private String operationType;
    @TableField("old_value")
    private String oldValue;
    @TableField("new_value")
    private String newValue;
    @TableField("operator_id")
    private Long operatorId;
    @TableField("operator_name")
    private String operatorName;
    @TableField("create_time")
    private LocalDateTime createTime;
}
