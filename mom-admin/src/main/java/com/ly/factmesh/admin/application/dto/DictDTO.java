package com.ly.factmesh.admin.application.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字典DTO
 *
 * @author 屈想顺
 */
@Data
public class DictDTO {
    
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