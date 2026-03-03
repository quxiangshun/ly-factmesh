package com.ly.factmesh.ops.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemEventDTO {
    private Long id;
    private String eventType;
    private Integer eventLevel;
    private String eventContent;
    private String relatedService;
    private String relatedId;
    private LocalDateTime createTime;
    private Integer processed;
    private LocalDateTime processTime;
}
