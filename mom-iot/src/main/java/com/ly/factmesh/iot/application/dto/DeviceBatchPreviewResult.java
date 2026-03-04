package com.ly.factmesh.iot.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 设备批量导入预览结果
 *
 * @author LY-FactMesh
 */
@Data
@Builder
@Schema(description = "设备批量导入预览结果")
public class DeviceBatchPreviewResult {

    @Schema(description = "可导入的有效行（设备编码、设备名称非空）")
    private List<DeviceImportRow> rows;

    @Schema(description = "校验失败的行及原因")
    private List<RowError> errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "单行设备数据（用于预览与导入）")
    public static class DeviceImportRow {
        @Schema(description = "Excel 行号（从 1 起）")
        private int row;
        @Schema(description = "设备编码")
        private String deviceCode;
        @Schema(description = "设备名称")
        private String deviceName;
        @Schema(description = "设备类型")
        private String deviceType;
        @Schema(description = "型号")
        private String model;
        @Schema(description = "制造商")
        private String manufacturer;
        @Schema(description = "安装位置")
        private String installLocation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "单行校验错误")
    public static class RowError {
        @Schema(description = "Excel 行号（从 1 起）")
        private int row;
        @Schema(description = "设备编码")
        private String deviceCode;
        @Schema(description = "错误原因")
        private String message;
    }
}
