package com.ly.factmesh.admin.application.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 角色DTO
 *
 * @author 屈想顺
 */
@Data
public class RoleDTO {
    
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