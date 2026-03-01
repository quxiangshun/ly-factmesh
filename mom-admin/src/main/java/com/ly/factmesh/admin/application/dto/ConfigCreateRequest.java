package com.ly.factmesh.admin.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建配置请求DTO
 *
 * @author 屈想顺
 */
@Data
public class ConfigCreateRequest {
    
    /**
     * 配置键
     */
    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过100个字符")
    private String configKey;
    
    /**
     * 配置值
     */
    @NotBlank(message = "配置值不能为空")
    private String configValue;
    
    /**
     * 配置描述
     */
    @Size(max = 500, message = "配置描述长度不能超过500个字符")
    private String configDesc;
    
    /**
     * 状态
     */
    private Integer status;
}