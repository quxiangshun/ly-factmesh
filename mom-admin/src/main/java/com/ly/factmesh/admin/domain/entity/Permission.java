package com.ly.factmesh.admin.domain.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 权限实体
 *
 * @author 屈想顺
 */
@Data
public class Permission {
    
    /**
     * 权限ID
     */
    private Long id;
    
    /**
     * 权限名称
     */
    private String permName;
    
    /**
     * 权限代码
     */
    private String permCode;
    
    /**
     * URL路径
     */
    private String url;
    
    /**
     * HTTP方法
     */
    private String method;
    
    /**
     * 父权限ID
     */
    private Long parentId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}