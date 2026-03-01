package com.ly.factmesh.admin.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.domain.entity.Permission;

import java.util.List;
import java.util.Optional;

/**
 * 权限仓库接口
 *
 * @author 屈想顺
 */
public interface PermissionRepository {
    
    /**
     * 保存权限
     * @param permission 权限实体
     * @return 保存后的权限实体
     */
    Permission save(Permission permission);
    
    /**
     * 根据ID查询权限
     * @param id 权限ID
     * @return 可选的权限实体
     */
    Optional<Permission> findById(Long id);
    
    /**
     * 根据权限代码查询权限
     * @param permCode 权限代码
     * @return 可选的权限实体
     */
    Optional<Permission> findByPermCode(String permCode);
    
    /**
     * 根据父权限ID查询子权限
     * @param parentId 父权限ID
     * @return 子权限列表
     */
    List<Permission> findByParentId(Long parentId);
    
    /**
     * 查询所有权限
     * @return 权限列表
     */
    List<Permission> findAll();
    
    /**
     * 分页查询权限
     * @param page 页码
     * @param size 每页大小
     * @return 权限分页列表
     */
    Page<Permission> findByPage(Integer page, Integer size);
    
    /**
     * 根据ID删除权限
     * @param id 权限ID
     */
    void deleteById(Long id);
    
    /**
     * 统计权限数量
     * @return 权限数量
     */
    long count();
}