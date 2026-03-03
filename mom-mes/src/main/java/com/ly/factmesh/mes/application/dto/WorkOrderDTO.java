package com.ly.factmesh.mes.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单 DTO
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "工单")
public class WorkOrderDTO {

    @Schema(description = "工单ID")
    private Long id;
    @Schema(description = "工单编码")
    private String orderCode;
    @Schema(description = "产品编码")
    private String productCode;
    @Schema(description = "产品名称")
    private String productName;
    @Schema(description = "计划数量")
    private Integer planQuantity;
    @Schema(description = "实际数量")
    private Integer actualQuantity;
    @Schema(description = "状态：0草稿 1已下发 2进行中 3已完成 4已关闭 5暂停")
    private Integer status;
    @Schema(description = "产线ID")
    private Long lineId;
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
