package com.ly.factmesh.admin.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 租户创建请求
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "租户创建请求")
public class TenantCreateRequest {

    @NotBlank(message = "租户编码不能为空")
    @Schema(description = "租户编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tenantCode;

    @NotBlank(message = "租户名称不能为空")
    @Schema(description = "租户名称", requiredMode = Schema.RequiredMode.REQUIRED)
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
