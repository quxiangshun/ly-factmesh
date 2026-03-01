package com.ly.factmesh.admin.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.application.dto.PermissionCreateRequest;
import com.ly.factmesh.admin.application.dto.PermissionDTO;
import com.ly.factmesh.admin.application.dto.PermissionUpdateRequest;
import com.ly.factmesh.admin.application.service.PermissionApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

/**
 * 权限控制器
 *
 * @author 屈想顺
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "权限相关的CRUD操作")
public class PermissionController {
    
    private final PermissionApplicationService permissionApplicationService;
    
    /**
     * 创建权限
     * @param request 创建权限请求
     * @return 创建成功的权限信息
     */
    @PostMapping
    @Operation(summary = "创建权限", description = "创建新的权限信息")
    public ResponseEntity<PermissionDTO> createPermission(@Valid @RequestBody PermissionCreateRequest request) {
        PermissionDTO permissionDTO = permissionApplicationService.createPermission(request);
        return new ResponseEntity<>(permissionDTO, HttpStatus.CREATED);
    }
    
    /**
     * 根据ID查询权限
     * @param id 权限ID
     * @return 权限信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询权限", description = "根据权限ID查询权限信息")
    public ResponseEntity<PermissionDTO> getPermissionById(@PathVariable @Parameter(description = "权限ID") Long id) {
        PermissionDTO permissionDTO = permissionApplicationService.getPermissionById(id);
        return ResponseEntity.ok(permissionDTO);
    }
    
    /**
     * 分页查询权限
     * @param page 页码
     * @param size 每页大小
     * @return 权限分页列表
     */
    @GetMapping
    @Operation(summary = "分页查询权限", description = "分页查询权限列表")
    public ResponseEntity<Page<PermissionDTO>> getPermissions(@RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
                                                              @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size) {
        Page<PermissionDTO> permissionPage = permissionApplicationService.getPermissions(page, size);
        return ResponseEntity.ok(permissionPage);
    }
    
    /**
     * 更新权限
     * @param id 权限ID
     * @param request 更新权限请求
     * @return 更新后的权限信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新权限", description = "根据权限ID更新权限信息")
    public ResponseEntity<PermissionDTO> updatePermission(@PathVariable @Parameter(description = "权限ID") Long id, @Valid @RequestBody PermissionUpdateRequest request) {
        PermissionDTO permissionDTO = permissionApplicationService.updatePermission(id, request);
        return ResponseEntity.ok(permissionDTO);
    }
    
    /**
     * 删除权限
     * @param id 权限ID
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限", description = "根据权限ID删除权限")
    public ResponseEntity<Void> deletePermission(@PathVariable @Parameter(description = "权限ID") Long id) {
        permissionApplicationService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 检查权限是否存在
     * @param id 权限ID
     * @return 是否存在
     */
    @GetMapping("/{id}/exists")
    @Operation(summary = "检查权限是否存在", description = "根据权限ID检查权限是否存在")
    public ResponseEntity<Boolean> checkPermissionExists(@PathVariable @Parameter(description = "权限ID") Long id) {
        Boolean exists = permissionApplicationService.checkPermissionExists(id);
        return ResponseEntity.ok(exists);
    }

    /**
     * 获取权限树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取权限树", description = "获取树形结构的权限列表")
    public ResponseEntity<java.util.List<com.ly.factmesh.admin.application.dto.PermissionTreeDTO>> getPermissionTree() {
        return ResponseEntity.ok(permissionApplicationService.getPermissionTree());
    }
}