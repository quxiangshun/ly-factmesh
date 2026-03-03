package com.ly.factmesh.qms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "质检任务关联的 NCR 创建上下文（用于从任务快速创建不合格品）")
public class InspectionTaskNcrContextDTO {

    private Long taskId;
    private String taskCode;
    private Long orderId;
    private Long materialId;
    private String suggestedProductCode;  // 物料ID 转字符串，供参考
}
