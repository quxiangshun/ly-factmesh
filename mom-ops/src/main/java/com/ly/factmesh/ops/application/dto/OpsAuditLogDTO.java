package com.ly.factmesh.ops.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OpsAuditLogDTO {
    private Long id;
    private String serviceName;
    private Long userId;
    private String username;
    private String operationType;
    private String operationContent;
    private Integer operationResult;
    private LocalDateTime operationTime;
    private String clientIp;
    private String requestParams;
}
