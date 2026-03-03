package com.ly.factmesh.mes.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单数据库实体
 *
 * @author LY-FactMesh
 */
@Data
@TableName("work_order")
public class WorkOrderEntity {

    @TableId("id")
    private Long id;
    @TableField("order_code")
    private String orderCode;
    @TableField("product_code")
    private String productCode;
    @TableField("product_name")
    private String productName;
    @TableField("plan_quantity")
    private Integer planQuantity;
    @TableField("actual_quantity")
    private Integer actualQuantity;
    @TableField("status")
    private Integer status;
    @TableField("line_id")
    private Long lineId;
    @TableField("start_time")
    private LocalDateTime startTime;
    @TableField("end_time")
    private LocalDateTime endTime;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
