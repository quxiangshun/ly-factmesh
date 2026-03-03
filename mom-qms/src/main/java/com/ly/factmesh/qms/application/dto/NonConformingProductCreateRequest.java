package com.ly.factmesh.qms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "不合格品创建请求")
public class NonConformingProductCreateRequest {

    @NotBlank(message = "产品编码不能为空")
    @Schema(description = "产品编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productCode;

    @Schema(description = "批次号")
    private String batchNo;

    @NotNull(message = "数量不能为空")
    @Schema(description = "不合格数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @NotBlank(message = "不合格原因不能为空")
    @Schema(description = "不合格原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @Schema(description = "处置方式：0待处置 1返工 2报废 3让步接收 4退货")
    private Integer disposalMethod;

    @Schema(description = "关联质检任务ID")
    private Long taskId;

    @Schema(description = "备注")
    private String remark;
}
