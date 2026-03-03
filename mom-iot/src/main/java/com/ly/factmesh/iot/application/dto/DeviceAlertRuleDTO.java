package com.ly.factmesh.iot.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备告警规则 DTO
 *
 * @author LY-FactMesh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备告警规则")
public class DeviceAlertRuleDTO {

    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "设备ID（为空则按设备类型或全部匹配）")
    private Long deviceId;

    @Schema(description = "设备类型（为空则匹配全部）")
    private String deviceType;

    @Schema(description = "测点名称")
    private String fieldName;

    @Schema(description = "操作符：gt/gte/lt/lte/eq/ne/between/outside")
    private String operator;

    @Schema(description = "阈值（between/outside 时为下限）")
    private Double thresholdValue;

    @Schema(description = "阈值上限（between/outside 时使用）")
    private Double thresholdValueHigh;

    @Schema(description = "告警类型")
    private String alertType;

    @Schema(description = "告警级别：1-4")
    private Integer alertLevel;

    @Schema(description = "告警内容模板，支持 {value} {threshold}")
    private String alertContentTemplate;

    @Schema(description = "是否启用：0-否 1-是")
    private Integer enabled;

    @Schema(description = "冷却时间（秒），同规则同设备在此时间内不重复告警")
    private Integer cooldownSeconds;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
