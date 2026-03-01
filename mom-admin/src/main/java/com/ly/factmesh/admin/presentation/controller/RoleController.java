package com.ly.factmesh.admin.presentation.controller;

import com.ly.factmesh.admin.application.dto.RoleCreateRequest;
import com.ly.factmesh.admin.application.dto.RoleDTO;
import com.ly.factmesh.admin.application.dto.RoleUpdateRequest;
import com.ly.factmesh.admin.application.service.RoleApplicationService;
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
 * 角色控制器
 *
 * @author 屈想顺
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色相关的CRUD操作")
public class RoleController {
    
    private final RoleApplicationService roleApplicationService;
    
    /**
     * 创建角色
     * @param request 创建角色请求
     * @return 创建成功的角色信息
     */
    @PostMapping
    @Operation(summary = "创建角色", description = "创建新的角色信息")
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleCreateRequest request) {
        RoleDTO roleDTO = roleApplicationService.createRole(request);
        return new ResponseEntity<>(roleDTO, HttpStatus.CREATED);
    }
    
    /**
     * 根据ID查询角色
     * @param id 角色ID
     * @return 角色信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询角色", description = "根据角色ID查询角色信息")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable @Parameter(description = "角色ID") Long id) {
        RoleDTO roleDTO = roleApplicationService.getRoleById(id);
        return ResponseEntity.ok(roleDTO);
    }
    
    /**
     * 分页查询角色
     * @param page 页码
     * @param size 每页大小
     * @return 角色分页列表
     */
    @GetMapping
    @Operation(summary = "分页查询角色", description = "分页查询角色列表")
    public ResponseEntity<Page<RoleDTO>> getRoles(@RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
                                                 @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size) {
        Page<RoleDTO> rolePage = roleApplicationService.getRoles(page, size);
        return ResponseEntity.ok(rolePage);
    }
    
    /**
     * 更新角色
     * @param id 角色ID
     * @param request 更新角色请求
     * @return 更新后的角色信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新角色", description = "根据角色ID更新角色信息")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable @Parameter(description = "角色ID") Long id, @Valid @RequestBody RoleUpdateRequest request) {
        RoleDTO roleDTO = roleApplicationService.updateRole(id, request);
        return ResponseEntity.ok(roleDTO);
    }
    
    /**
     * 删除角色
     * @param id 角色ID
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "根据角色ID删除角色")
    public ResponseEntity<Void> deleteRole(@PathVariable @Parameter(description = "角色ID") Long id) {
        roleApplicationService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}