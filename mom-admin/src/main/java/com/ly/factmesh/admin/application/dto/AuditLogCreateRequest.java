package com.ly.factmesh.admin.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审计日志创建请求
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "审计日志创建请求")
public class AuditLogCreateRequest {

    @NotBlank(message = "表名不能为空")
    @Schema(description = "表名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tableName;

    @Schema(description = "记录ID")
    private String recordId;

    @NotNull(message = "操作类型不能为空")
    @Schema(description = "操作类型：create/update/delete", requiredMode = Schema.RequiredMode.REQUIRED)
    private String operationType;

    @Schema(description = "变更前值(JSON)")
    private String oldValue;

    @Schema(description = "变更后值(JSON)")
    private String newValue;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;
}
