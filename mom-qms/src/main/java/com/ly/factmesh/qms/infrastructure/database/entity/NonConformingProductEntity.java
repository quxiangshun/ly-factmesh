package com.ly.factmesh.qms.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("non_conforming_product")
public class NonConformingProductEntity {

    @TableId("id")
    private Long id;
    @TableField("ncr_no")
    private String ncrNo;
    @TableField("product_code")
    private String productCode;
    @TableField("batch_no")
    private String batchNo;
    @TableField("quantity")
    private Integer quantity;
    @TableField("reason")
    private String reason;
    @TableField("disposal_method")
    private Integer disposalMethod;
    @TableField("disposal_result")
    private Integer disposalResult;
    @TableField("task_id")
    private Long taskId;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("dispose_time")
    private LocalDateTime disposeTime;
    @TableField("remark")
    private String remark;
}
