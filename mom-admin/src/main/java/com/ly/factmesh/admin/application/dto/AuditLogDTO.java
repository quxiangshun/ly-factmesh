package com.ly.factmesh.admin.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志 DTO
 *
 * @author LY-FactMesh
 */
@Data
public class AuditLogDTO {

    private Long id;
    private String tableName;
    private String recordId;
    private String operationType;
    private String oldValue;
    private String newValue;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createTime;
}
