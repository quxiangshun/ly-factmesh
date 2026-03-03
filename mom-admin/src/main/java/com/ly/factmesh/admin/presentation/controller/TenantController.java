package com.ly.factmesh.admin.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.application.dto.TenantCreateRequest;
import com.ly.factmesh.admin.application.dto.TenantDTO;
import com.ly.factmesh.admin.application.dto.TenantUpdateRequest;
import com.ly.factmesh.admin.application.service.TenantApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 租户 REST 控制器
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Tag(name = "租户管理", description = "多租户数据隔离、租户配置")
public class TenantController {

    private final TenantApplicationService tenantApplicationService;

    @PostMapping
    @Operation(summary = "创建租户")
    public ResponseEntity<TenantDTO> create(@Valid @RequestBody TenantCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantApplicationService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询租户")
    public ResponseEntity<TenantDTO> getById(@PathVariable @Parameter(description = "租户ID") Long id) {
        return ResponseEntity.ok(tenantApplicationService.getById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询租户")
    public ResponseEntity<Page<TenantDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size
    ) {
        return ResponseEntity.ok(tenantApplicationService.page(page, size));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新租户")
    public ResponseEntity<TenantDTO> update(
            @PathVariable Long id,
            @RequestBody TenantUpdateRequest request
    ) {
        return ResponseEntity.ok(tenantApplicationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除租户")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tenantApplicationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
