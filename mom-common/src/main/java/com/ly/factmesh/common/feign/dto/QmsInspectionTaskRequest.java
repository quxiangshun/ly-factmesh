package com.ly.factmesh.common.feign.dto;

import lombok.Data;

/**
 * 质检任务创建请求（跨服务 Feign 契约，与 QMS InspectionTaskCreateRequest 字段一致）
 *
 * @author LY-FactMesh
 */
@Data
public class QmsInspectionTaskRequest {

    private String taskCode;
    private Long orderId;
    private Long materialId;
    private Long deviceId;
    private Integer inspectionType;
}
