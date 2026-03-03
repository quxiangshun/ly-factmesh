package com.ly.factmesh.mes.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工序 DTO
 *
 * @author LY-FactMesh
 */
@Data
public class ProcessDTO {

    private Long id;
    private String processCode;
    private String processName;
    private Integer sequence;
    private String workCenter;
    private LocalDateTime createTime;
}
