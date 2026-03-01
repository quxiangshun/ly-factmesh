package com.ly.factmesh.admin.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新角色请求DTO
 *
 * @author 屈想顺
 */
@Data
public class RoleUpdateRequest {
    
    /**
     * 角色名称
     */
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String roleName;
    
    /**
     * 角色代码
     */
    @Size(max = 50, message = "角色代码长度不能超过50个字符")
    private String roleCode;
    
    /**
     * 描述
     */
    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;
}