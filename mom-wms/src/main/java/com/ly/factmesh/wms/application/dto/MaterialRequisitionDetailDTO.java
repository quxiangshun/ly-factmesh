package com.ly.factmesh.wms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 领料单明细 DTO
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "领料单明细")
public class MaterialRequisitionDetailDTO {

    @Schema(description = "明细ID")
    private Long id;
    @Schema(description = "领料单ID")
    private Long reqId;
    @Schema(description = "物料ID")
    private Long materialId;
    @Schema(description = "物料编码")
    private String materialCode;
    @Schema(description = "物料名称")
    private String materialName;
    @Schema(description = "批次号")
    private String batchNo;
    @Schema(description = "申请数量")
    private Integer quantity;
    @Schema(description = "实发数量")
    private Integer actualQuantity;
}
