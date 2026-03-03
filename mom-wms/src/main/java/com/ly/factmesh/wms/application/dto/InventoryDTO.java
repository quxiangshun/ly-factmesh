package com.ly.factmesh.wms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存 DTO
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "库存")
public class InventoryDTO {

    @Schema(description = "库存ID")
    private Long id;
    @Schema(description = "物料ID")
    private Long materialId;
    @Schema(description = "物料编码")
    private String materialCode;
    @Schema(description = "物料名称")
    private String materialName;
    @Schema(description = "仓库")
    private String warehouse;
    @Schema(description = "库位")
    private String location;
    @Schema(description = "数量")
    private Integer quantity;
    @Schema(description = "安全库存")
    private Integer safeStock;
    @Schema(description = "是否低于安全库存")
    private Boolean belowSafeStock;
    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdateTime;
}
