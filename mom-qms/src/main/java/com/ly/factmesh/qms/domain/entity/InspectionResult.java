package com.ly.factmesh.qms.domain.entity;

import java.time.LocalDateTime;

/**
 * 质检结果领域实体
 *
 * @author LY-FactMesh
 */
public class InspectionResult {

    private Long id;
    private Long taskId;
    private String inspectionItem;
    private String standardValue;
    private String actualValue;
    private Integer judgment;
    private String inspector;
    private LocalDateTime inspectionTime;
    private LocalDateTime createTime;

    /** 判定结果：0合格 1不合格 */
    public static final int JUDGMENT_PASS = 0;
    public static final int JUDGMENT_FAIL = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getInspectionItem() { return inspectionItem; }
    public void setInspectionItem(String inspectionItem) { this.inspectionItem = inspectionItem; }
    public String getStandardValue() { return standardValue; }
    public void setStandardValue(String standardValue) { this.standardValue = standardValue; }
    public String getActualValue() { return actualValue; }
    public void setActualValue(String actualValue) { this.actualValue = actualValue; }
    public Integer getJudgment() { return judgment; }
    public void setJudgment(Integer judgment) { this.judgment = judgment; }
    public String getInspector() { return inspector; }
    public void setInspector(String inspector) { this.inspector = inspector; }
    public LocalDateTime getInspectionTime() { return inspectionTime; }
    public void setInspectionTime(LocalDateTime inspectionTime) { this.inspectionTime = inspectionTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
