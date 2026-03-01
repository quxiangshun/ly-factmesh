package com.ly.factmesh.admin.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色数据库实体
 *
 * @author 屈想顺
 */
@Data
@TableName("sys_role")
public class RoleEntity {
    
    /**
     * 角色ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 角色名称
     */
    @TableField(value = "role_name")
    private String roleName;
    
    /**
     * 角色代码
     */
    @TableField(value = "role_code")
    private String roleCode;
    
    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}