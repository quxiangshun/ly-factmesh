package com.ly.factmesh.mes.application.dto;

import lombok.Data;

/**
 * 工单统计 DTO
 *
 * @author LY-FactMesh
 */
@Data
public class WorkOrderStatsDTO {

    private long total;
    private long draftCount;
    private long releasedCount;
    private long inProgressCount;
    private long completedCount;
}
