package com.ly.factmesh.wms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 库存调整请求
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "库存调整请求")
public class InventoryAdjustRequest {

    @NotNull
    @Schema(description = "物料ID")
    private Long materialId;
    @Schema(description = "批次号，可选")
    private String batchNo;
    @NotNull
    @Schema(description = "数量，正数入库、负数出库")
    private Integer quantity;
    @Schema(description = "仓库，默认空")
    private String warehouse;
    @Schema(description = "库位，默认空")
    private String location;
    @Schema(description = "参考单号")
    private String referenceNo;
    @Schema(description = "操作人")
    private String operator;
    @Schema(description = "关联工单ID，用于追溯")
    private Long orderId;
    @Schema(description = "关联领料单ID，用于追溯")
    private Long reqId;
}
