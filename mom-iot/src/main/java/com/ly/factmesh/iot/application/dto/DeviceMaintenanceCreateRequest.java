package com.ly.factmesh.iot.application.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 设备维保记录创建请求
 *
 * @author LY-FactMesh
 */
@Data
public class DeviceMaintenanceCreateRequest {

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotNull(message = "维保类型不能为空")
    private String maintenanceType;

    @NotNull(message = "维保日期不能为空")
    private LocalDate maintenanceDate;

    private String content;
    private String operatorName;
    private BigDecimal cost;
    private String remark;
}
