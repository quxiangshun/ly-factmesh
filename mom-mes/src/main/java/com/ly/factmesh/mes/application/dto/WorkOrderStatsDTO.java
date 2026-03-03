package com.ly.factmesh.mes.application.dto;

import lombok.Data;

/**
 * 工单统计 DTO
 *
 * @author LY-FactMesh
 */
@Data
public class WorkOrderStatsDTO {

    /** 工单总数 */
    private long total;
    /** 草稿数量 */
    private long draftCount;
    /** 已下发数量 */
    private long releasedCount;
    /** 进行中数量 */
    private long inProgressCount;
    /** 暂停数量 */
    private long pausedCount;
    /** 已完成数量 */
    private long completedCount;
}
