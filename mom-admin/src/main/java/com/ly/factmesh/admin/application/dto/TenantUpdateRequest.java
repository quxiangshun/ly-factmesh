package com.ly.factmesh.admin.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 租户更新请求
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "租户更新请求")
public class TenantUpdateRequest {

    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "联系人")
    private String contact;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "状态：0禁用 1启用")
    private Integer status;

    @Schema(description = "租户配置(JSON)")
    private String config;
}
