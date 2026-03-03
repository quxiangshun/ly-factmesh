package com.ly.factmesh.qms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "质检结果创建请求")
public class InspectionResultCreateRequest {

    @NotNull(message = "任务ID不能为空")
    @Schema(description = "质检任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long taskId;

    @NotBlank(message = "检验项不能为空")
    @Schema(description = "检验项名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String inspectionItem;

    @Schema(description = "标准值")
    private String standardValue;

    @Schema(description = "实际值")
    private String actualValue;

    @NotNull(message = "判定结果不能为空")
    @Schema(description = "判定：0合格 1不合格", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer judgment;

    @Schema(description = "检验员")
    private String inspector;
}
