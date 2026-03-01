package com.ly.factmesh.admin.domain.repository;

import com.ly.factmesh.admin.domain.entity.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Optional;


/**
 * 用户仓库接口
 *
 * @author 屈想顺
 */
public interface UserRepository {
    
    /**
     * 保存用户
     * @param user 用户实体
     * @return 保存后的用户实体
     */
    User save(User user);
    
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 可选的用户实体
     */
    Optional<User> findById(Long id);
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 可选的用户实体
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> findAll();
    
    /**
     * 分页查询用户
     * @param page 页码
     * @param size 每页大小
     * @return 用户分页列表
     */
    Page<User> findByPage(Integer page, Integer size);
    
    /**
     * 根据ID删除用户
     * @param id 用户ID
     */
    void deleteById(Long id);
    
    /**
     * 统计用户数量
     * @return 用户数量
     */
    long count();
}