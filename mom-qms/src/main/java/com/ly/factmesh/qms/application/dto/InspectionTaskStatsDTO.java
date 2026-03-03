package com.ly.factmesh.qms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "质检任务统计")
public class InspectionTaskStatsDTO {

    private long total;
    private long draftCount;
    private long inProgressCount;
    private long completedCount;
}
