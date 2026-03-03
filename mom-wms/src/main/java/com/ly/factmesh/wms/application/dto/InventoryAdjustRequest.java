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
}
