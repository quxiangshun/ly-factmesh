package com.ly.factmesh.iot.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备信息更新请求
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "设备信息更新请求")
public class DeviceUpdateRequest {

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "设备型号")
    private String model;

    @Schema(description = "制造商")
    private String manufacturer;

    @Schema(description = "安装位置")
    private String installLocation;
}
