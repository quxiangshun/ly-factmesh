package com.ly.factmesh.wms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 出入库记录 DTO
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "出入库记录")
public class InventoryTransactionDTO {

    @Schema(description = "记录ID")
    private Long id;
    @Schema(description = "物料ID")
    private Long materialId;
    @Schema(description = "物料编码")
    private String materialCode;
    @Schema(description = "物料名称")
    private String materialName;
    @Schema(description = "类型：1-入库 2-出库 3-调整")
    private Integer transactionType;
    @Schema(description = "数量")
    private Integer quantity;
    @Schema(description = "仓库")
    private String warehouse;
    @Schema(description = "库位")
    private String location;
    @Schema(description = "交易时间")
    private LocalDateTime transactionTime;
    @Schema(description = "操作人")
    private String operator;
    @Schema(description = "参考单号")
    private String referenceNo;
}
