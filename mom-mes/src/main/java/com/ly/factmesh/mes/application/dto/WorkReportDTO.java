package com.ly.factmesh.mes.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报工记录 DTO
 *
 * @author LY-FactMesh
 */
@Data
public class WorkReportDTO {

    private Long id;
    private Long orderId;
    private Long processId;
    private Long deviceId;
    private Integer reportQuantity;
    private Integer scrapQuantity;
    private LocalDateTime reportTime;
    private String operator;
    /** 工单编码（冗余展示） */
    private String orderCode;
    /** 工序名称（冗余展示） */
    private String processName;
}
