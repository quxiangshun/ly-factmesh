package com.ly.factmesh.common.feign.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 领料单创建请求（跨服务 Feign 契约）
 *
 * @author LY-FactMesh
 */
@Data
public class RequisitionCreateRequest {

    @NotNull(message = "工单ID不能为空")
    private Long workOrderId;
    private String workOrderNo;
    private String materialCode;
    private String materialName;
    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须大于0")
    private Integer quantity;
}
