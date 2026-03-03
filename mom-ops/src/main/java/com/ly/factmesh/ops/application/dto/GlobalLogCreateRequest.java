package com.ly.factmesh.ops.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GlobalLogCreateRequest {
    @NotBlank
    private String serviceName;
    @NotNull
    private Integer logType;
    private String logLevel;
    @NotBlank
    private String logContent;
    private String requestId;
    private String clientIp;
}
