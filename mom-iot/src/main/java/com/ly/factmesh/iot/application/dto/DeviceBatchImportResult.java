package com.ly.factmesh.iot.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 设备批量导入结果
 *
 * @author LY-FactMesh
 */
@Data
@Builder
@Schema(description = "设备批量导入结果")
public class DeviceBatchImportResult {

    @Schema(description = "成功数量")
    private int successCount;

    @Schema(description = "失败数量")
    private int failCount;

    @Schema(description = "失败明细（行号 + 原因）")
    private List<RowError> errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "单行导入错误")
    public static class RowError {
        @Schema(description = "Excel 行号（从 1 起）")
        private int row;

        @Schema(description = "设备编码")
        private String deviceCode;

        @Schema(description = "错误原因")
        private String message;
    }
}
