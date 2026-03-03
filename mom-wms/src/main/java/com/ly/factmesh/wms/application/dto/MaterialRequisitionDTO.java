package com.ly.factmesh.wms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 领料单 DTO
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "领料单")
public class MaterialRequisitionDTO {

    @Schema(description = "领料单ID")
    private Long id;
    @Schema(description = "领料单号")
    private String reqNo;
    @Schema(description = "工单ID")
    private Long orderId;
    @Schema(description = "类型：1-领料 2-退料")
    private Integer reqType;
    @Schema(description = "状态：0-草稿 1-已提交 2-已完成")
    private Integer status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "明细列表")
    private List<MaterialRequisitionDetailDTO> details;
}
