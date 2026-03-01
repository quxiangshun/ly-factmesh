package com.ly.factmesh.admin.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配置数据库实体
 *
 * @author 屈想顺
 */
@Data
@TableName("sys_config")
public class ConfigEntity {
    
    /**
     * 配置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 配置键
     */
    @TableField(value = "config_key")
    private String configKey;
    
    /**
     * 配置值
     */
    @TableField(value = "config_value")
    private String configValue;
    
    /**
     * 配置描述
     */
    @TableField(value = "config_desc")
    private String configDesc;
    
    /**
     * 状态
     */
    @TableField(value = "status")
    private Integer status;
    
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