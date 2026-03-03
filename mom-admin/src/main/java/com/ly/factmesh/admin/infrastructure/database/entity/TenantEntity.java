package com.ly.factmesh.admin.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户数据库实体
 *
 * @author LY-FactMesh
 */
@Data
@TableName("sys_tenant")
public class TenantEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("tenant_code")
    private String tenantCode;
    @TableField("tenant_name")
    private String tenantName;
    @TableField("contact")
    private String contact;
    @TableField("phone")
    private String phone;
    @TableField("status")
    private Integer status;
    @TableField("config")
    private String config;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
