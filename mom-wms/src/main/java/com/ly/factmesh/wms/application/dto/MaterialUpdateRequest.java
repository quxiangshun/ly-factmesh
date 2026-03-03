package com.ly.factmesh.wms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "物料更新请求")
public class MaterialUpdateRequest {

    @NotBlank(message = "物料名称不能为空")
    @Schema(description = "物料名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialName;

    @Schema(description = "物料类型")
    private String materialType;

    @Schema(description = "规格")
    private String specification;

    @Schema(description = "单位")
    private String unit;
}
