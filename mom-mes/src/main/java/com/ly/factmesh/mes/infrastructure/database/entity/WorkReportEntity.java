package com.ly.factmesh.mes.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报工记录数据库实体
 *
 * @author LY-FactMesh
 */
@Data
@TableName("work_report")
public class WorkReportEntity {

    @TableId("id")
    private Long id;
    @TableField("order_id")
    private Long orderId;
    @TableField("process_id")
    private Long processId;
    @TableField("device_id")
    private Long deviceId;
    @TableField("report_quantity")
    private Integer reportQuantity;
    @TableField("scrap_quantity")
    private Integer scrapQuantity;
    @TableField("report_time")
    private LocalDateTime reportTime;
    @TableField("operator")
    private String operator;
}
