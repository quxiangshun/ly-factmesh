package com.ly.factmesh.admin.application.service;

import com.ly.factmesh.admin.application.dto.UserCreateRequest;
import com.ly.factmesh.admin.application.dto.UserDTO;
import com.ly.factmesh.admin.application.dto.UserUpdateRequest;
import com.ly.factmesh.admin.domain.entity.User;
import com.ly.factmesh.admin.domain.repository.UserRepository;
import org.springframework.beans.BeanUtils;
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
        user.setPassword(request.getPassword()); // 实际项目中需要加密密码
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
            user.setPassword(request.getPassword()); // 实际项目中需要加密密码
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
        // 检查用户是否存在
        userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 删除用户
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
     * 将用户实体转换为DTO
     * @param user 用户实体
     * @return 用户DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}