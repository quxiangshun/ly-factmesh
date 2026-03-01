package com.ly.factmesh.admin.domain.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 配置实体
 *
 * @author 屈想顺
 */
@Data
public class Config {
    
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