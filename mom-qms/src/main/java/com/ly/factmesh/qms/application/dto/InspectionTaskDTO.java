package com.ly.factmesh.qms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "质检任务")
public class InspectionTaskDTO {

    private Long id;
    private String taskCode;
    private Long orderId;
    private String orderCode;
    private String productCode;
    private Long materialId;
    private Long deviceId;
    private Integer inspectionType;
    private LocalDateTime inspectionTime;
    private Integer status;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
