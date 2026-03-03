package com.ly.factmesh.qms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "不合格品")
public class NonConformingProductDTO {

    private Long id;
    private String ncrNo;
    private String productCode;
    private String batchNo;
    private Integer quantity;
    private String reason;
    private Integer disposalMethod;
    private Integer disposalResult;
    private Long taskId;
    private LocalDateTime createTime;
    private LocalDateTime disposeTime;
    private String remark;
}
