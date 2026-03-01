package com.ly.factmesh.admin.domain.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 字典实体
 *
 * @author 屈想顺
 */
@Data
public class Dict {
    
    /**
     * 字典ID
     */
    private Long id;
    
    /**
     * 字典类型
     */
    private String dictType;
    
    /**
     * 字典代码
     */
    private String dictCode;
    
    /**
     * 字典名称
     */
    private String dictName;
    
    /**
     * 字典值
     */
    private String dictValue;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}