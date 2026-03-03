package com.ly.factmesh.qms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "质检结果")
public class InspectionResultDTO {

    private Long id;
    private Long taskId;
    private String inspectionItem;
    private String standardValue;
    private String actualValue;
    private Integer judgment;
    private String inspector;
    private LocalDateTime inspectionTime;
    private LocalDateTime createTime;
}
