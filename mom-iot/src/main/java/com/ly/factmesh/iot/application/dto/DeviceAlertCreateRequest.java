package com.ly.factmesh.iot.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建设备告警请求
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "创建设备告警")
public class DeviceAlertCreateRequest {

    @NotNull
    @Schema(description = "设备ID", required = true)
    private Long deviceId;

    @Schema(description = "设备编码")
    private String deviceCode;

    @NotNull
    @Schema(description = "告警类型", required = true, example = "TEMPERATURE_HIGH")
    private String alertType;

    @NotNull
    @Schema(description = "告警级别：1-信息 2-警告 3-错误 4-严重", required = true)
    private Integer alertLevel;

    @NotNull
    @Schema(description = "告警内容", required = true)
    private String alertContent;

    @Schema(description = "备注")
    private String remark;
}
