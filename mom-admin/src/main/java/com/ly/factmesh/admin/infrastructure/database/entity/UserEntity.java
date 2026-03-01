package com.ly.factmesh.admin.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户数据库实体
 *
 * @author 屈想顺
 */
@Data
@TableName("sys_user")
public class UserEntity {
    
    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名
     */
    @TableField(value = "username")
    private String username;
    
    /**
     * 密码
     */
    @TableField(value = "password")
    private String password;
    
    /**
     * 真实姓名
     */
    @TableField(value = "nickname")
    private String nickname;
    
    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;
    
    /**
     * 手机
     */
    @TableField(value = "phone")
    private String phone;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @TableField(value = "status")
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 创建者ID
     */

}