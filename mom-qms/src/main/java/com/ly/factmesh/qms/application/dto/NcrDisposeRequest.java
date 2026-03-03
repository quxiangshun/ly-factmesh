package com.ly.factmesh.qms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "不合格品处置请求")
public class NcrDisposeRequest {

    @Schema(description = "处置方式：0待处置 1返工 2报废 3让步接收 4退货", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer disposalMethod;

    @Schema(description = "处置说明/备注")
    private String remark;
}
