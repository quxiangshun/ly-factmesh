package com.ly.factmesh.mes.domain.entity;

import java.time.LocalDateTime;

/**
 * 报工记录领域实体
 *
 * @author LY-FactMesh
 */
public class WorkReport {

    private Long id;
    private Long orderId;
    private Long processId;
    private Long deviceId;
    private Integer reportQuantity;
    private Integer scrapQuantity;
    private LocalDateTime reportTime;
    private String operator;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProcessId() { return processId; }
    public void setProcessId(Long processId) { this.processId = processId; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    public Integer getReportQuantity() { return reportQuantity; }
    public void setReportQuantity(Integer reportQuantity) { this.reportQuantity = reportQuantity; }
    public Integer getScrapQuantity() { return scrapQuantity; }
    public void setScrapQuantity(Integer scrapQuantity) { this.scrapQuantity = scrapQuantity; }
    public LocalDateTime getReportTime() { return reportTime; }
    public void setReportTime(LocalDateTime reportTime) { this.reportTime = reportTime; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
}
