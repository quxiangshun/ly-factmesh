package com.ly.factmesh.common.feign.dto;

import lombok.Data;

/**
 * 领料单创建请求（跨服务 Feign 契约）
 *
 * @author LY-FactMesh
 */
@Data
public class RequisitionCreateRequest {

    private Long workOrderId;
    private String workOrderNo;
    private String materialCode;
    private String materialName;
    private Integer quantity;
}
