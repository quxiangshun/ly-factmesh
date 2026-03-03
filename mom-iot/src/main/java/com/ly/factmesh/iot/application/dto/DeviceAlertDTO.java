package com.ly.factmesh.iot.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备告警 DTO
 *
 * @author LY-FactMesh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备告警")
public class DeviceAlertDTO {

    @Schema(description = "告警ID")
    private Long id;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "规则ID（自动告警时有值）")
    private Long ruleId;

    @Schema(description = "告警类型")
    private String alertType;

    @Schema(description = "告警级别：1-信息 2-警告 3-错误 4-严重")
    private Integer alertLevel;

    @Schema(description = "告警内容")
    private String alertContent;

    @Schema(description = "告警状态：0-待处理 1-已处理")
    private Integer alertStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "处理时间")
    private LocalDateTime resolveTime;

    @Schema(description = "处理人")
    private String resolvedBy;

    @Schema(description = "备注")
    private String remark;
}
