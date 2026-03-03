package com.ly.factmesh.ops.application.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GlobalLogDTO {
    private Long id;
    private String serviceName;
    private Integer logType;
    private String logLevel;
    private String logContent;
    private String requestId;
    private String clientIp;
    private LocalDateTime createTime;
}
