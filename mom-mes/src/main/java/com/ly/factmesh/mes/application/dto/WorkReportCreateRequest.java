package com.ly.factmesh.mes.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

/**
 * 报工创建请求
 *
 * @author LY-FactMesh
 */
@Data
public class WorkReportCreateRequest {

    @NotNull(message = "工单ID不能为空")
    private Long orderId;

    @NotNull(message = "工序ID不能为空")
    private Long processId;

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotNull(message = "报工数量不能为空")
    @PositiveOrZero(message = "报工数量不能为负数")
    private Integer reportQuantity;

    @PositiveOrZero(message = "报废数量不能为负数")
    private Integer scrapQuantity = 0;

    private String operator;
}
