package com.ly.factmesh.qms.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 质量追溯数据库实体
 *
 * @author LY-FactMesh
 */
@Data
@TableName("quality_traceability")
public class QualityTraceabilityEntity {

    @TableId("id")
    private Long id;
    @TableField("product_code")
    private String productCode;
    @TableField("batch_no")
    private String batchNo;
    @TableField("material_batch")
    private String materialBatch;
    @TableField("production_order")
    private String productionOrder;
    @TableField("inspection_record_id")
    private Long inspectionRecordId;
    @TableField("non_conforming_id")
    private Long nonConformingId;
    @TableField("create_time")
    private LocalDateTime createTime;
}
