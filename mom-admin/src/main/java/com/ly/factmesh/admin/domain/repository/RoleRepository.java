package com.ly.factmesh.admin.domain.repository;

import com.ly.factmesh.admin.domain.entity.Role;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Optional;

/**
 * 角色仓库接口
 *
 * @author 屈想顺
 */
public interface RoleRepository {
    
    /**
     * 保存角色
     * @param role 角色实体
     * @return 保存后的角色实体
     */
    Role save(Role role);
    
    /**
     * 根据ID查询角色
     * @param id 角色ID
     * @return 可选的角色实体
     */
    Optional<Role> findById(Long id);
    
    /**
     * 根据角色名称查询角色
     * @param roleName 角色名称
     * @return 可选的角色实体
     */
    Optional<Role> findByRoleName(String roleName);
    
    /**
     * 根据角色代码查询角色
     * @param roleCode 角色代码
     * @return 可选的角色实体
     */
    Optional<Role> findByRoleCode(String roleCode);
    
    /**
     * 查询所有角色
     * @return 角色列表
     */
    List<Role> findAll();
    
    /**
     * 分页查询角色
     * @param page 页码
     * @param size 每页大小
     * @return 角色分页列表
     */
    Page<Role> findByPage(Integer page, Integer size);
    
    /**
     * 根据ID删除角色
     * @param id 角色ID
     */
    void deleteById(Long id);
    
    /**
     * 统计角色数量
     * @return 角色数量
     */
    long count();
}