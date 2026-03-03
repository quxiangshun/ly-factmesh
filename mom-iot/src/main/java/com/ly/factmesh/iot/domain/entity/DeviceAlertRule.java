package com.ly.factmesh.iot.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备告警规则实体（阈值自动告警）
 *
 * @author LY-FactMesh
 */
@Data
@TableName("device_alert_rule")
public class DeviceAlertRule {

    @TableId("id")
    private Long id;

    @TableField("rule_name")
    private String ruleName;

    @TableField("device_id")
    private Long deviceId;

    @TableField("device_type")
    private String deviceType;

    @TableField("field_name")
    private String fieldName;

    @TableField("operator")
    private String operator;

    @TableField("threshold_value")
    private Double thresholdValue;

    @TableField("threshold_value_high")
    private Double thresholdValueHigh;

    @TableField("alert_type")
    private String alertType;

    @TableField("alert_level")
    private Integer alertLevel;

    @TableField("alert_content_template")
    private String alertContentTemplate;

    @TableField("enabled")
    private Integer enabled;

    @TableField("cooldown_seconds")
    private Integer cooldownSeconds;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    /** 操作符：大于 */
    public static final String OP_GT = "gt";
    /** 操作符：大于等于 */
    public static final String OP_GTE = "gte";
    /** 操作符：小于 */
    public static final String OP_LT = "lt";
    /** 操作符：小于等于 */
    public static final String OP_LTE = "lte";
    /** 操作符：等于 */
    public static final String OP_EQ = "eq";
    /** 操作符：不等于 */
    public static final String OP_NE = "ne";
    /** 操作符：区间内（low <= value <= high，需 thresholdValue 与 thresholdValueHigh） */
    public static final String OP_BETWEEN = "between";
    /** 操作符：区间外（value < low 或 value > high） */
    public static final String OP_OUTSIDE = "outside";
}
