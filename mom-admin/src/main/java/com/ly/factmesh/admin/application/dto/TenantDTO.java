package com.ly.factmesh.admin.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户 DTO
 *
 * @author LY-FactMesh
 */
@Data
public class TenantDTO {

    private Long id;
    private String tenantCode;
    private String tenantName;
    private String contact;
    private String phone;
    private Integer status;
    private String config;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
