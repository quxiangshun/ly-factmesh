package com.ly.factmesh.admin.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新配置请求DTO
 *
 * @author 屈想顺
 */
@Data
public class ConfigUpdateRequest {
    
    /**
     * 配置键
     */
    @Size(max = 100, message = "配置键长度不能超过100个字符")
    private String configKey;
    
    /**
     * 配置值
     */
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