package com.ly.factmesh.iot.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备维保记录 DTO
 *
 * @author LY-FactMesh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceMaintenanceDTO {

    private Long id;
    private Long deviceId;
    private String deviceCode;
    private String deviceName;
    private String maintenanceType;
    private LocalDate maintenanceDate;
    private String content;
    private String operatorName;
    private BigDecimal cost;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
