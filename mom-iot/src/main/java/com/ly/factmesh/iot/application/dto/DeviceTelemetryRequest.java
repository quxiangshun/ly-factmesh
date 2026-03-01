package com.ly.factmesh.iot.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * 设备遥测数据上报请求
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "设备遥测数据上报")
public class DeviceTelemetryRequest {

    @NotNull
    @Schema(description = "设备ID", required = true)
    private Long deviceId;

    @Schema(description = "设备编码（可选，用于校验）")
    private String deviceCode;

    @Schema(description = "采集时间戳（毫秒），不传则使用服务端时间")
    private Long timestamp;

    @Schema(description = "遥测数据：key 为测点名称，value 为数值", example = "{\"temperature\": 25.5, \"humidity\": 60.2, \"voltage\": 220.0}")
    private Map<String, Number> data;

    public Instant getCollectTime() {
        return timestamp != null ? Instant.ofEpochMilli(timestamp) : Instant.now();
    }
}
