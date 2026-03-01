package com.ly.factmesh.admin.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.application.dto.PermissionCreateRequest;
import com.ly.factmesh.admin.application.dto.PermissionDTO;
import com.ly.factmesh.admin.application.dto.PermissionTreeDTO;
import com.ly.factmesh.admin.application.dto.PermissionUpdateRequest;
import com.ly.factmesh.admin.domain.entity.Permission;
import com.ly.factmesh.admin.domain.repository.PermissionRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限应用服务
 *
 * @author 屈想顺
 */
@Service
@RequiredArgsConstructor
public class PermissionApplicationService {
    
    private final PermissionRepository permissionRepository;
    
    /**
     * 创建权限
     * @param request 创建权限请求
     * @return 创建成功的权限DTO
     */
    @Transactional
    public PermissionDTO createPermission(PermissionCreateRequest request) {
        // 创建权限实体
        Permission permission = new Permission();
        permission.setPermName(request.getPermName());
        permission.setPermCode(request.getPermCode());
        permission.setUrl(request.getUrl());
        permission.setMethod(request.getMethod());
        permission.setParentId(request.getParentId());
        
        // 保存权限
        Permission savedPermission = permissionRepository.save(permission);
        
        // 转换为DTO返回
        return convertToDTO(savedPermission);
    }
    
    /**
     * 根据ID查询权限
     * @param id 权限ID
     * @return 权限DTO
     */
    public PermissionDTO getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("权限不存在"));
        
        return convertToDTO(permission);
    }
    
    /**
     * 分页查询权限
     * @param page 页码
     * @param size 每页大小
     * @return 权限DTO分页列表
     */
    public Page<PermissionDTO> getPermissions(Integer page, Integer size) {
        // 调用仓库层的分页查询
        Page<Permission> permissionPage = permissionRepository.findByPage(page, size);
        
        // 转换为DTO分页对象
        Page<PermissionDTO> dtoPage = new Page<>(page, size);
        dtoPage.setTotal(permissionPage.getTotal());
        dtoPage.setRecords(permissionPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList()));
        
        return dtoPage;
    }
    
    /**
     * 更新权限
     * @param id 权限ID
     * @param request 更新权限请求
     * @return 更新后的权限DTO
     */
    @Transactional
    public PermissionDTO updatePermission(Long id, PermissionUpdateRequest request) {
        // 查询权限
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("权限不存在"));
        
        // 更新权限信息
        if (request.getPermName() != null) {
            permission.setPermName(request.getPermName());
        }
        
        if (request.getPermCode() != null) {
            permission.setPermCode(request.getPermCode());
        }
        
        if (request.getUrl() != null) {
            permission.setUrl(request.getUrl());
        }
        
        if (request.getMethod() != null) {
            permission.setMethod(request.getMethod());
        }
        
        if (request.getParentId() != null) {
            permission.setParentId(request.getParentId());
        }
        
        // 保存更新
        Permission updatedPermission = permissionRepository.save(permission);
        
        return convertToDTO(updatedPermission);
    }
    
    /**
     * 删除权限
     * @param id 权限ID
     */
    @Transactional
    public void deletePermission(Long id) {
        // 检查权限是否存在
        permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("权限不存在"));
        
        // 删除权限
        permissionRepository.deleteById(id);
    }
    
    /**
     * 检查权限是否存在
     * @param id 权限ID
     * @return 是否存在
     */
    public Boolean checkPermissionExists(Long id) {
        return permissionRepository.findById(id).isPresent();
    }

    /**
     * 获取权限树（parent_id 为 null 的为根节点）
     */
    public List<PermissionTreeDTO> getPermissionTree() {
        List<Permission> all = permissionRepository.findAll();
        return buildTree(all, null);
    }

    private List<PermissionTreeDTO> buildTree(List<Permission> all, Long parentId) {
        List<PermissionTreeDTO> result = new ArrayList<>();
        for (Permission p : all) {
            boolean match = (parentId == null && p.getParentId() == null)
                    || (parentId != null && parentId.equals(p.getParentId()));
            if (!match) continue;
            PermissionTreeDTO node = new PermissionTreeDTO();
            node.setId(p.getId());
            node.setPermName(p.getPermName());
            node.setPermCode(p.getPermCode());
            node.setUrl(p.getUrl());
            node.setMethod(p.getMethod());
            node.setParentId(p.getParentId());
            node.setCreateTime(p.getCreateTime());
            node.setChildren(buildTree(all, p.getId()));
            result.add(node);
        }
        return result;
    }
    
    /**
     * 将权限实体转换为DTO
     * @param permission 权限实体
     * @return 权限DTO
     */
    private PermissionDTO convertToDTO(Permission permission) {
        PermissionDTO dto = new PermissionDTO();
        BeanUtils.copyProperties(permission, dto);
        return dto;
    }
}