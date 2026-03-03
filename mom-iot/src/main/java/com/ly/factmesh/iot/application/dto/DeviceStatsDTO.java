package com.ly.factmesh.iot.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备统计 DTO
 *
 * @author LY-FactMesh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备统计")
public class DeviceStatsDTO {

    @Schema(description = "设备总数")
    private long total;

    @Schema(description = "在线设备数")
    private long online;

    @Schema(description = "故障设备数")
    private long fault;
}
