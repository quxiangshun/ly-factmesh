package com.ly.factmesh.wms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 手动创建领料单请求（草稿）
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "手动创建领料单请求")
public class RequisitionManualCreateRequest {

    @NotNull(message = "物料ID不能为空")
    @Schema(description = "物料ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long materialId;

    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须大于0")
    @Schema(description = "申请数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @Schema(description = "工单ID（可选）")
    private Long orderId;

    @Schema(description = "类型：1-领料 2-退料，默认1")
    private Integer reqType;
}
