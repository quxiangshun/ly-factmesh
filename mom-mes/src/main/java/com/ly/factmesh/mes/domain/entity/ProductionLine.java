package com.ly.factmesh.mes.domain.entity;

import com.ly.factmesh.common.enums.ProductionLineStatusEnum;

import java.time.LocalDateTime;

/**
 * 产线领域实体
 * 状态码参见 {@link ProductionLineStatusEnum}
 *
 * @author LY-FactMesh
 */
public class ProductionLine {

    /** 主键 */
    private Long id;
    /** 产线编码，唯一 */
    private String lineCode;
    /** 产线名称 */
    private String lineName;
    /** 描述 */
    private String description;
    /** 排序号，用于展示顺序 */
    private Integer sequence;
    /** 状态：0-空闲 1-运行 2-检修，参见 ProductionLineStatusEnum */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLineCode() { return lineCode; }
    public void setLineCode(String lineCode) { this.lineCode = lineCode; }
    public String getLineName() { return lineName; }
    public void setLineName(String lineName) { this.lineName = lineName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSequence() { return sequence; }
    public void setSequence(Integer sequence) { this.sequence = sequence; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
