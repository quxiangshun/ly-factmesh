package com.ly.factmesh.iot.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建设备告警规则请求
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "创建设备告警规则")
public class DeviceAlertRuleCreateRequest {

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "设备ID（可选，为空则按设备类型或全部匹配）")
    private Long deviceId;

    @Schema(description = "设备类型（可选）")
    private String deviceType;

    @NotBlank(message = "测点名称不能为空")
    @Schema(description = "测点名称", required = true, example = "temperature")
    private String fieldName;

    @NotBlank(message = "操作符不能为空")
    @Schema(description = "操作符：gt/gte/lt/lte/eq/ne/between/outside", required = true, example = "gt")
    private String operator;

    @NotNull(message = "阈值不能为空")
    @Schema(description = "阈值（between/outside 时为下限）", required = true, example = "80.0")
    private Double thresholdValue;

    @Schema(description = "阈值上限（between/outside 时必填）", example = "100.0")
    private Double thresholdValueHigh;

    @NotBlank(message = "告警类型不能为空")
    @Schema(description = "告警类型", required = true, example = "TEMPERATURE_HIGH")
    private String alertType;

    @NotNull(message = "告警级别不能为空")
    @Schema(description = "告警级别：1-4", required = true)
    private Integer alertLevel;

    @Schema(description = "告警内容模板，支持 {value} {threshold}")
    private String alertContentTemplate;

    @Schema(description = "是否启用：0-否 1-是", defaultValue = "1")
    private Integer enabled;

    @Schema(description = "冷却时间（秒）", defaultValue = "300")
    private Integer cooldownSeconds;
}
