package com.ly.factmesh.admin.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报表定义 DTO
 */
@Data
@Schema(description = "报表定义")
public class ReportDefDTO {

    @Schema(description = "ID")
    private Long id;
    @NotBlank(message = "报表名称不能为空")
    @Size(max = 100)
    @Schema(description = "报表名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @NotBlank(message = "报表类型不能为空")
    @Size(max = 50)
    @Schema(description = "报表类型：production_summary/capacity/device_stats/inspection_stats", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reportType;
    @Schema(description = "默认参数 JSON，如 {\"date\":\"2025-03-03\"}")
    private String paramsJson;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
