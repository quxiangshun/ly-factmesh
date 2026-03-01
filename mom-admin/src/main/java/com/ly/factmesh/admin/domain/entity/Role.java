package com.ly.factmesh.admin.domain.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 角色实体
 *
 * @author 屈想顺
 */
@Data
public class Role {
    
    /**
     * 角色ID
     */
    private Long id;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色代码
     */
    private String roleCode;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}