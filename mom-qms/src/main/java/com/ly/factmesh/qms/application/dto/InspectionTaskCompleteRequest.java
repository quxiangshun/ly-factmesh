package com.ly.factmesh.qms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "质检任务完成请求")
public class InspectionTaskCompleteRequest {

    @Schema(description = "检验员/操作员")
    private String operator;

    @Schema(description = "是否强制完成（存在不合格项时仍允许完成）")
    private Boolean forceComplete;
}
