package com.ly.factmesh.admin.application.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 配置DTO
 *
 * @author 屈想顺
 */
@Data
public class ConfigDTO {
    
    /**
     * 配置ID
     */
    private Long id;
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置描述
     */
    private String configDesc;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}