package com.ly.factmesh.admin.application.service;

import com.ly.factmesh.admin.application.dto.RoleDTO;
import com.ly.factmesh.admin.application.dto.UserCreateRequest;
import com.ly.factmesh.admin.application.dto.UserDTO;
import com.ly.factmesh.admin.application.dto.UserUpdateRequest;
import com.ly.factmesh.admin.domain.entity.Role;
import com.ly.factmesh.admin.domain.entity.User;
import com.ly.factmesh.admin.domain.repository.RoleRepository;
import com.ly.factmesh.admin.domain.repository.UserRepository;
import com.ly.factmesh.admin.infrastructure.database.mapper.UserRoleMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 用户应用服务
 *
 * @author 屈想顺
 */
@Service
@RequiredArgsConstructor
public class UserApplicationService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMapper userRoleMapper;
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    
    /**
     * 创建用户
     * @param request 创建用户请求
     * @return 用户DTO
     */
    @Transactional
    public UserDTO createUser(UserCreateRequest request) {
        // 检查用户名是否已存在
        userRepository.findByUsername(request.getUsername())
                .ifPresent(user -> {
                    throw new RuntimeException("用户名已存在");
                });
        
        // 创建用户实体
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PASSWORD_ENCODER.encode(request.getPassword()));
        user.setNickname(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        
        // 设置状态，默认启用
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        // 保存用户
        User savedUser = userRepository.save(user);
        
        // 转换为DTO返回
        return convertToDTO(savedUser);
    }
    
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户DTO
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return convertToDTO(user);
    }
    
    /**
     * 分页查询用户
     * @param page 页码
     * @param size 每页大小
     * @return 用户DTO分页列表
     */
    public Page<UserDTO> getUsers(Integer page, Integer size) {
        // 调用仓库层的分页查询
        Page<User> userPage = userRepository.findByPage(page, size);
        
        // 转换为DTO分页对象
        Page<UserDTO> dtoPage = new Page<>(page, size);
        dtoPage.setTotal(userPage.getTotal());
        dtoPage.setRecords(userPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        
        return dtoPage;
    }
    
    /**
     * 更新用户
     * @param id 用户ID
     * @param request 更新用户请求
     * @return 更新后的用户DTO
     */
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateRequest request) {
        // 查询用户
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 更新用户信息
        if (request.getRealName() != null) {
            user.setNickname(request.getRealName());
        }
        
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(PASSWORD_ENCODER.encode(request.getPassword()));
        }
        
        // 保存更新
        User updatedUser = userRepository.save(user);
        
        return convertToDTO(updatedUser);
    }
    
    /**
     * 删除用户
     * @param id 用户ID
     */
    @Transactional
    public void deleteUser(Long id) {
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        userRoleMapper.deleteByUserId(id);
        userRepository.deleteById(id);
    }
    
    /**
     * 检查用户是否存在
     * @param id 用户ID
     * @return 是否存在
     */
    public Boolean checkUserExists(Long id) {
        return userRepository.findById(id).isPresent();
    }

    /**
     * 获取用户的角色列表
     */
    public List<RoleDTO> getUserRoles(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        List<Long> roleIds = userRoleMapper.findRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) return List.of();
        return roleIds.stream()
                .map(roleRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(o -> convertRoleToDTO(o.get()))
                .collect(Collectors.toList());
    }

    /**
     * 为用户分配角色
     */
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        if (roleIds == null || roleIds.isEmpty()) {
            userRoleMapper.deleteByUserId(userId);
            return;
        }
        for (Long roleId : roleIds) {
            roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("角色不存在: " + roleId));
        }
        userRoleMapper.deleteByUserId(userId);
        userRoleMapper.insertBatch(userId, roleIds);
    }

    private RoleDTO convertRoleToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setRoleName(role.getRoleName());
        dto.setRoleCode(role.getRoleCode());
        dto.setDescription(role.getDescription());
        dto.setCreateTime(role.getCreateTime());
        dto.setUpdateTime(role.getUpdateTime());
        return dto;
    }
    
    /**
     * 将用户实体转换为DTO
     * @param user 用户实体
     * @return 用户DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setRealName(user.getNickname());
        return dto;
    }
}