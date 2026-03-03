package com.ly.factmesh.iot.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备维保记录实体（设备台账扩展）
 *
 * @author LY-FactMesh
 */
@Data
@TableName("device_maintenance")
public class DeviceMaintenance {

    @TableId("id")
    private Long id;

    @TableField("device_id")
    private Long deviceId;

    /** 维保类型：保养、维修、点检 */
    @TableField("maintenance_type")
    private String maintenanceType;

    @TableField("maintenance_date")
    private LocalDate maintenanceDate;

    @TableField("content")
    private String content;

    @TableField("operator_name")
    private String operatorName;

    @TableField("cost")
    private BigDecimal cost;

    @TableField("remark")
    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    public static final String TYPE_MAINTENANCE = "maintenance";
    public static final String TYPE_REPAIR = "repair";
    public static final String TYPE_INSPECTION = "inspection";
}
