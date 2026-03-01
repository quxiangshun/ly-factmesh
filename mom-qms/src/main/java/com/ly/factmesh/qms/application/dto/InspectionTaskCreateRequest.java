package com.ly.factmesh.qms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "质检任务创建请求")
public class InspectionTaskCreateRequest {

    @NotBlank(message = "任务编码不能为空")
    @Schema(description = "任务编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String taskCode;

    @Schema(description = "工单ID")
    private Long orderId;

    @Schema(description = "物料ID")
    private Long materialId;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "检验类型")
    private Integer inspectionType;
}
