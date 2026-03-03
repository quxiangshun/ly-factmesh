package com.ly.factmesh.mes.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

/**
 * 产线更新请求
 *
 * @author LY-FactMesh
 */
@Data
public class ProductionLineUpdateRequest {

    @NotBlank(message = "产线名称不能为空")
    private String lineName;

    private String description;

    @NotNull(message = "排序号不能为空")
    @PositiveOrZero(message = "排序号不能为负数")
    private Integer sequence = 0;
}
