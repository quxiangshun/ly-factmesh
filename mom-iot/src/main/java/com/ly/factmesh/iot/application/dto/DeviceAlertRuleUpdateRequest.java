package com.ly.factmesh.iot.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新设备告警规则请求
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "更新设备告警规则")
public class DeviceAlertRuleUpdateRequest {

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "测点名称")
    private String fieldName;

    @Schema(description = "操作符：gt/gte/lt/lte/eq/ne/between/outside")
    private String operator;

    @Schema(description = "阈值（between/outside 时为下限）")
    private Double thresholdValue;

    @Schema(description = "阈值上限（between/outside 时必填）")
    private Double thresholdValueHigh;

    @Schema(description = "告警类型")
    private String alertType;

    @Schema(description = "告警级别：1-4")
    private Integer alertLevel;

    @Schema(description = "告警内容模板")
    private String alertContentTemplate;

    @Schema(description = "是否启用：0-否 1-是")
    private Integer enabled;

    @Schema(description = "冷却时间（秒）")
    private Integer cooldownSeconds;
}
