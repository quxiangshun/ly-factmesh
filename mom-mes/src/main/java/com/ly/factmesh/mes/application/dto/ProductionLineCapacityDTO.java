package com.ly.factmesh.mes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 产线产能统计 DTO
 *
 * @author LY-FactMesh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionLineCapacityDTO {

    private Long lineId;
    private String lineCode;
    private String lineName;
    private Integer status;
    private LocalDate date;
    private Long completedOrderCount;
    private Integer completedQuantity;
}
