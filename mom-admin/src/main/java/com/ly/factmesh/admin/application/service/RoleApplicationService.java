package com.ly.factmesh.admin.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.application.dto.RoleCreateRequest;
import com.ly.factmesh.admin.application.dto.RoleDTO;
import com.ly.factmesh.admin.application.dto.RoleUpdateRequest;
import com.ly.factmesh.admin.domain.entity.Role;
import com.ly.factmesh.admin.domain.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色应用服务
 *
 * @author 屈想顺
 */
@Service
@RequiredArgsConstructor
public class RoleApplicationService {
    
    private final RoleRepository roleRepository;
    
    /**
     * 创建角色
     * @param request 创建角色请求
     * @return 创建成功的角色DTO
     */
    @Transactional
    public RoleDTO createRole(RoleCreateRequest request) {
        // 创建角色实体
        Role role = new Role();
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
        
        // 保存角色
        Role savedRole = roleRepository.save(role);
        
        // 转换为DTO返回
        return convertToDTO(savedRole);
    }
    
    /**
     * 根据ID查询角色
     * @param id 角色ID
     * @return 角色DTO
     */
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        
        return convertToDTO(role);
    }
    
    /**
     * 查询所有角色
     * @return 角色DTO列表
     */
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        
        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 分页查询角色
     * @param page 页码
     * @param size 每页大小
     * @return 角色DTO分页列表
     */
    public Page<RoleDTO> getRoles(Integer page, Integer size) {
        // 调用仓库层的分页查询
        Page<Role> rolePage = roleRepository.findByPage(page, size);
        
        // 转换为DTO分页对象
        Page<RoleDTO> dtoPage = new Page<>(page, size);
        dtoPage.setTotal(rolePage.getTotal());
        dtoPage.setRecords(rolePage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        
        return dtoPage;
    }
    
    /**
     * 更新角色
     * @param id 角色ID
     * @param request 更新角色请求
     * @return 更新后的角色DTO
     */
    @Transactional
    public RoleDTO updateRole(Long id, RoleUpdateRequest request) {
        // 查询角色
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        
        // 更新角色信息
        if (request.getRoleName() != null) {
            role.setRoleName(request.getRoleName());
        }
        
        if (request.getRoleCode() != null) {
            role.setRoleCode(request.getRoleCode());
        }
        
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        
        // 保存更新
        Role updatedRole = roleRepository.save(role);
        
        return convertToDTO(updatedRole);
    }
    
    /**
     * 删除角色
     * @param id 角色ID
     */
    @Transactional
    public void deleteRole(Long id) {
        // 检查角色是否存在
        roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        
        // 删除角色
        roleRepository.deleteById(id);
    }
    
    /**
     * 将角色实体转换为DTO
     * @param role 角色实体
     * @return 角色DTO
     */
    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setRoleName(role.getRoleName());
        dto.setRoleCode(role.getRoleCode());
        dto.setDescription(role.getDescription());
        dto.setCreateTime(role.getCreateTime());
        dto.setUpdateTime(role.getUpdateTime());
        return dto;
    }
}