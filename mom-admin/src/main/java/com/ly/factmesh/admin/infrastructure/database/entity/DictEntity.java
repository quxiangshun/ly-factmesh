package com.ly.factmesh.admin.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典数据库实体
 *
 * @author 屈想顺
 */
@Data
@TableName("sys_dict")
public class DictEntity {
    
    /**
     * 字典ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 字典类型
     */
    @TableField(value = "dict_type")
    private String dictType;
    
    /**
     * 字典代码
     */
    @TableField(value = "dict_code")
    private String dictCode;
    
    /**
     * 字典名称
     */
    @TableField(value = "dict_name")
    private String dictName;
    
    /**
     * 字典值
     */
    @TableField(value = "dict_value")
    private String dictValue;
    
    /**
     * 排序
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;
    
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
}