package com.ly.factmesh.wms.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "物料")
public class MaterialDTO {

    private Long id;
    private String materialCode;
    private String materialName;
    private String materialType;
    private String specification;
    private String unit;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
