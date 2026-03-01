package com.ly.factmesh.iot.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 设备注册请求
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "设备注册请求")
public class DeviceRegisterRequest {

    @NotBlank(message = "设备编码不能为空")
    @Schema(description = "设备编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED)
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
