package com.ly.factmesh.admin.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建角色请求DTO
 *
 * @author 屈想顺
 */
@Data
public class RoleCreateRequest {
    
    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String roleName;
    
    /**
     * 角色代码
     */
    @NotBlank(message = "角色代码不能为空")
    @Size(max = 50, message = "角色代码长度不能超过50个字符")
    private String roleCode;
    
    /**
     * 描述
     */
    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;
}