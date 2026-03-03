package com.ly.factmesh.wms.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.wms.application.dto.MaterialCreateRequest;
import com.ly.factmesh.wms.application.dto.MaterialDTO;
import com.ly.factmesh.wms.application.dto.MaterialUpdateRequest;
import com.ly.factmesh.wms.application.service.MaterialApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 物料 REST 控制器
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
@Tag(name = "物料管理", description = "物料创建、查询、分页、删除")
public class MaterialController {

    private final MaterialApplicationService materialApplicationService;

    @PostMapping
    @Operation(summary = "创建物料")
    public ResponseEntity<MaterialDTO> create(@Valid @RequestBody MaterialCreateRequest request) {
        MaterialDTO dto = materialApplicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询物料")
    public ResponseEntity<MaterialDTO> getById(@PathVariable @Parameter(description = "物料ID") Long id) {
        return ResponseEntity.ok(materialApplicationService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新物料")
    public ResponseEntity<MaterialDTO> update(
            @PathVariable @Parameter(description = "物料ID") Long id,
            @Valid @RequestBody MaterialUpdateRequest request
    ) {
        return ResponseEntity.ok(materialApplicationService.update(id, request));
    }

    @GetMapping
    @Operation(summary = "分页查询物料")
    public ResponseEntity<Page<MaterialDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size,
            @RequestParam(required = false) @Parameter(description = "物料编码（模糊）") String materialCode,
            @RequestParam(required = false) @Parameter(description = "物料名称（模糊）") String materialName,
            @RequestParam(required = false) @Parameter(description = "物料类型") String materialType
    ) {
        return ResponseEntity.ok(materialApplicationService.page(page, size, materialCode, materialName, materialType));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除物料")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        materialApplicationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
