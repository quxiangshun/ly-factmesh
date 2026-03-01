package com.ly.factmesh.admin.presentation.controller;

import com.ly.factmesh.admin.application.dto.UserCreateRequest;
import com.ly.factmesh.admin.application.dto.UserDTO;
import com.ly.factmesh.admin.application.dto.UserUpdateRequest;
import com.ly.factmesh.admin.application.service.UserApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


/**
 * 用户控制器
 *
 * @author 屈想顺
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关的CRUD操作")
public class UserController {
    
    private final UserApplicationService userApplicationService;
    
    /**
     * 创建用户
     * @param request 创建用户请求
     * @return 创建成功的用户信息
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新的用户信息")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserDTO userDTO = userApplicationService.createUser(request);
        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }
    
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询用户", description = "根据用户ID查询用户信息")
    public ResponseEntity<UserDTO> getUserById(@PathVariable @Parameter(description = "用户ID") Long id) {
        UserDTO userDTO = userApplicationService.getUserById(id);
        return ResponseEntity.ok(userDTO);
    }
    
    /**
     * 分页查询用户
     * @param page 页码
     * @param size 每页大小
     * @return 用户分页列表
     */
    @GetMapping
    @Operation(summary = "分页查询用户", description = "分页查询用户列表")
    public ResponseEntity<Page<UserDTO>> getUsers(@RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
                                                 @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size) {
        Page<UserDTO> userPage = userApplicationService.getUsers(page, size);
        return ResponseEntity.ok(userPage);
    }
    
    /**
     * 更新用户
     * @param id 用户ID
     * @param request 更新用户请求
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "根据用户ID更新用户信息")
    public ResponseEntity<UserDTO> updateUser(@PathVariable @Parameter(description = "用户ID") Long id, @Valid @RequestBody UserUpdateRequest request) {
        UserDTO userDTO = userApplicationService.updateUser(id, request);
        return ResponseEntity.ok(userDTO);
    }
    
    /**
     * 删除用户
     * @param id 用户ID
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    public ResponseEntity<Void> deleteUser(@PathVariable @Parameter(description = "用户ID") Long id) {
        userApplicationService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 检查用户是否存在
     * @param id 用户ID
     * @return 是否存在
     */
    @GetMapping("/{id}/exists")
    @Operation(summary = "检查用户是否存在", description = "根据用户ID检查用户是否存在")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable @Parameter(description = "用户ID") Long id) {
        Boolean exists = userApplicationService.checkUserExists(id);
        return ResponseEntity.ok(exists);
    }
}