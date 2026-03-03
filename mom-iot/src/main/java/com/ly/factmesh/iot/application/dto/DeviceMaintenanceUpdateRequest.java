package com.ly.factmesh.iot.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 设备维保记录更新请求
 *
 * @author LY-FactMesh
 */
@Data
public class DeviceMaintenanceUpdateRequest {

    private String maintenanceType;
    private LocalDate maintenanceDate;
    private String content;
    private String operatorName;
    private BigDecimal cost;
    private String remark;
}
