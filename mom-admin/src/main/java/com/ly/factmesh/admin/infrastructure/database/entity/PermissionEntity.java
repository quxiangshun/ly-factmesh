package com.ly.factmesh.admin.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限数据库实体
 *
 * @author 屈想顺
 */
@Data
@TableName("sys_permission")
public class PermissionEntity {
    
    /**
     * 权限ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 权限名称
     */
    @TableField(value = "perm_name")
    private String permName;
    
    /**
     * 权限代码
     */
    @TableField(value = "perm_code")
    private String permCode;
    
    /**
     * URL路径
     */
    @TableField(value = "url")
    private String url;
    
    /**
     * HTTP方法
     */
    @TableField(value = "method")
    private String method;
    
    /**
     * 父权限ID
     */
    @TableField(value = "parent_id")
    private Long parentId;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}