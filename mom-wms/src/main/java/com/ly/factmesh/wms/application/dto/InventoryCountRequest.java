package com.ly.factmesh.wms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 盘点确认请求
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "盘点确认请求")
public class InventoryCountRequest {

    @NotNull
    @Schema(description = "库存记录ID")
    private Long inventoryId;
    @NotNull
    @Schema(description = "实盘数量")
    private Integer actualQuantity;
    @Schema(description = "操作人")
    private String operator;
}
