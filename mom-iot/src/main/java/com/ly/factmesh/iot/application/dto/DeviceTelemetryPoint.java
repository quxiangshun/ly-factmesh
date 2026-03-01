package com.ly.factmesh.iot.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 设备遥测数据点（查询结果）
 *
 * @author LY-FactMesh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "遥测数据点")
public class DeviceTelemetryPoint {

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "测点名称")
    private String field;

    @Schema(description = "数值")
    private Double value;

    @Schema(description = "采集时间")
    private Instant time;
}
