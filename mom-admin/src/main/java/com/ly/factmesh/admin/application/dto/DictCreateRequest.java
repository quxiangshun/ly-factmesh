package com.ly.factmesh.admin.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建字典请求DTO
 *
 * @author 屈想顺
 */
@Data
public class DictCreateRequest {
    
    /**
     * 字典类型
     */
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 50, message = "字典类型长度不能超过50个字符")
    private String dictType;
    
    /**
     * 字典代码
     */
    @NotBlank(message = "字典代码不能为空")
    @Size(max = 50, message = "字典代码长度不能超过50个字符")
    private String dictCode;
    
    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称长度不能超过100个字符")
    private String dictName;
    
    /**
     * 字典值
     */
    @NotBlank(message = "字典值不能为空")
    @Size(max = 200, message = "字典值长度不能超过200个字符")
    private String dictValue;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 状态
     */
    private Integer status;
}