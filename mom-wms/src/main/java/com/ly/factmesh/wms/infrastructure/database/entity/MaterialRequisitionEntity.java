package com.ly.factmesh.wms.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("material_requisition")
public class MaterialRequisitionEntity {

    @TableId("id")
    private Long id;
    @TableField("req_no")
    private String reqNo;
    @TableField("order_id")
    private Long orderId;
    @TableField("req_type")
    private Integer reqType;
    @TableField("status")
    private Integer status;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
