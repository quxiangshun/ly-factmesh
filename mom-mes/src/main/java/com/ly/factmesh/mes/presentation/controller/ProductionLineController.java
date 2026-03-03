package com.ly.factmesh.mes.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.mes.application.dto.ProductionLineCreateRequest;
import com.ly.factmesh.mes.application.dto.ProductionLineDTO;
import com.ly.factmesh.mes.application.dto.ProductionLineUpdateRequest;
import com.ly.factmesh.mes.application.service.ProductionLineApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产线 REST 控制器
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/lines")
@RequiredArgsConstructor
@Tag(name = "产线管理", description = "产线、工位定义 CRUD")
public class ProductionLineController {

    private final ProductionLineApplicationService productionLineApplicationService;

    @PostMapping
    @Operation(summary = "创建产线")
    public ResponseEntity<ProductionLineDTO> create(@Valid @RequestBody ProductionLineCreateRequest request) {
        ProductionLineDTO dto = productionLineApplicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询产线")
    public ResponseEntity<ProductionLineDTO> getById(@PathVariable @Parameter(description = "产线ID") Long id) {
        return ResponseEntity.ok(productionLineApplicationService.getById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询产线")
    public ResponseEntity<Page<ProductionLineDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size
    ) {
        return ResponseEntity.ok(productionLineApplicationService.page(page, size));
    }

    @GetMapping("/list")
    @Operation(summary = "获取全部产线列表（用于下拉选择）")
    public ResponseEntity<List<ProductionLineDTO>> listAll() {
        return ResponseEntity.ok(productionLineApplicationService.listAll());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新产线")
    public ResponseEntity<ProductionLineDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductionLineUpdateRequest request
    ) {
        return ResponseEntity.ok(productionLineApplicationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除产线")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productionLineApplicationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
