package com.ly.factmesh.admin.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志 DTO
 *
 * @author LY-FactMesh
 */
@Data
public class OperationLogDTO {

    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String operation;
    private String method;
    private String url;
    private String params;
    private String ip;
    private Integer status;
    private String errorMsg;
    private Long duration;
    private LocalDateTime createTime;
}
