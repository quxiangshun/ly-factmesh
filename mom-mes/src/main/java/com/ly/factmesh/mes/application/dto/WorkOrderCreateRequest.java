package com.ly.factmesh.mes.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 工单创建请求
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "工单创建请求")
public class WorkOrderCreateRequest {

    @NotBlank(message = "工单编码不能为空")
    @Schema(description = "工单编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderCode;

    @NotBlank(message = "产品编码不能为空")
    @Schema(description = "产品编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productCode;

    @NotBlank(message = "产品名称不能为空")
    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productName;

    @NotNull(message = "计划数量不能为空")
    @Positive(message = "计划数量必须大于0")
    @Schema(description = "计划数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer planQuantity;

    @Schema(description = "产线ID（可选，用于产线产能统计）")
    private Long lineId;
}
