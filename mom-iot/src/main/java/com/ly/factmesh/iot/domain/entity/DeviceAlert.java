package com.ly.factmesh.iot.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备告警实体
 *
 * @author LY-FactMesh
 */
@Data
@TableName("device_alert")
public class DeviceAlert {

    @TableId("id")
    private Long id;

    @TableField("device_id")
    private Long deviceId;

    @TableField("device_code")
    private String deviceCode;

    @TableField("rule_id")
    private Long ruleId;

    @TableField("alert_type")
    private String alertType;

    private static final int LEVEL_INFO = 1;
    private static final int LEVEL_WARNING = 2;
    private static final int LEVEL_ERROR = 3;
    private static final int LEVEL_CRITICAL = 4;

    @TableField("alert_level")
    private Integer alertLevel;

    @TableField("alert_content")
    private String alertContent;

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_RESOLVED = 1;

    @TableField("alert_status")
    private Integer alertStatus;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("resolve_time")
    private LocalDateTime resolveTime;

    @TableField("resolved_by")
    private String resolvedBy;

    @TableField("remark")
    private String remark;
}
