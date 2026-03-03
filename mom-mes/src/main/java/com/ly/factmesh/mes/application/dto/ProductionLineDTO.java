package com.ly.factmesh.mes.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产线 DTO
 *
 * @author LY-FactMesh
 */
@Data
public class ProductionLineDTO {

    private Long id;
    private String lineCode;
    private String lineName;
    private String description;
    private Integer sequence;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
