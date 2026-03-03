package com.ly.factmesh.mes.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

/**
 * 工序更新请求
 *
 * @author LY-FactMesh
 */
@Data
public class ProcessUpdateRequest {

    @NotBlank(message = "工序名称不能为空")
    private String processName;

    @NotNull(message = "排序号不能为空")
    @PositiveOrZero(message = "排序号不能为负数")
    private Integer sequence = 0;

    private String workCenter;
}
