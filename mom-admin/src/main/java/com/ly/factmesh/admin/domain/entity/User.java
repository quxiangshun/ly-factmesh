package com.ly.factmesh.admin.domain.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户实体
 *
 * @author 屈想顺
 */
@Getter
public class User {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机
     */
    private String phone;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @Setter
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Setter
    private LocalDateTime updatedAt;
    
    // 构造方法
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = 1; // 默认启用
    }
    
    public User(Long id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = 1; // 默认启用
    }
    
    // 自定义setter方法，包含更新updatedAt的逻辑
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setUsername(String username) {
        this.username = username;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setPassword(String password) {
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setStatus(Integer status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}