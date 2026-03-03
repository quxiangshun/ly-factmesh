package com.ly.factmesh.wms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 领料完成请求（实发数量）
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "领料完成请求")
public class RequisitionCompleteRequest {

    @Schema(description = "明细实发数量列表")
    private List<DetailActualQuantity> details;

    @Data
    @Schema(description = "明细实发数量")
    public static class DetailActualQuantity {
        @NotNull
        @Schema(description = "明细ID")
        private Long detailId;
        @NotNull
        @Schema(description = "实发数量")
        private Integer actualQuantity;
    }
}
