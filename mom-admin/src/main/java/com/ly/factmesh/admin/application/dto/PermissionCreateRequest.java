package com.ly.factmesh.admin.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建权限请求DTO
 *
 * @author 屈想顺
 */
@Data
public class PermissionCreateRequest {
    
    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    private String permName;
    
    /**
     * 权限代码
     */
    @NotBlank(message = "权限代码不能为空")
    @Size(max = 50, message = "权限代码长度不能超过50个字符")
    private String permCode;
    
    /**
     * URL路径
     */
    @Size(max = 200, message = "URL路径长度不能超过200个字符")
    private String url;
    
    /**
     * HTTP方法
     */
    @Size(max = 20, message = "HTTP方法长度不能超过20个字符")
    private String method;
    
    /**
     * 父权限ID
     */
    private Long parentId;
}