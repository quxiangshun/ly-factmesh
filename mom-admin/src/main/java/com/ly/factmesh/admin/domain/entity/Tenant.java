package com.ly.factmesh.admin.domain.entity;

import java.time.LocalDateTime;

/**
 * 租户领域实体
 *
 * @author LY-FactMesh
 */
public class Tenant {

    private Long id;
    private String tenantCode;
    private String tenantName;
    private String contact;
    private String phone;
    private Integer status;
    private String config;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }
    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
