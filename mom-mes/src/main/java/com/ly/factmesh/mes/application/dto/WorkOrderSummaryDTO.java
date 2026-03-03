package com.ly.factmesh.mes.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 生产汇总 DTO（简易生产报表）
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "生产汇总")
public class WorkOrderSummaryDTO {

    @Schema(description = "统计日期")
    private LocalDate date;
    @Schema(description = "当日完成工单数")
    private long completedCount;
    @Schema(description = "当日完成产量")
    private int completedQuantity;
    @Schema(description = "进行中工单数")
    private long inProgressCount;
    @Schema(description = "暂停工单数")
    private long pausedCount;
}
