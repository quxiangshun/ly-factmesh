package com.ly.factmesh.qms.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.qms.application.dto.NonConformingProductCreateRequest;
import com.ly.factmesh.qms.application.dto.NonConformingProductDTO;
import com.ly.factmesh.qms.application.service.NonConformingProductApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 不合格品（NCR）REST 控制器
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/ncr")
@RequiredArgsConstructor
@Tag(name = "不合格品管理", description = "不合格品登记、分页、处置、删除")
public class NonConformingProductController {

    private final NonConformingProductApplicationService nonConformingProductApplicationService;

    @PostMapping
    @Operation(summary = "创建不合格品记录")
    public ResponseEntity<NonConformingProductDTO> create(@Valid @RequestBody NonConformingProductCreateRequest request) {
        NonConformingProductDTO dto = nonConformingProductApplicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询不合格品")
    public ResponseEntity<NonConformingProductDTO> getById(@PathVariable @Parameter(description = "记录ID") Long id) {
        return ResponseEntity.ok(nonConformingProductApplicationService.getById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询不合格品")
    public ResponseEntity<Page<NonConformingProductDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size,
            @RequestParam(required = false) @Parameter(description = "处置结果：0待处理 1已处理") Integer disposalResult,
            @RequestParam(required = false) @Parameter(description = "关联质检任务ID") Long taskId
    ) {
        return ResponseEntity.ok(nonConformingProductApplicationService.page(page, size, disposalResult, taskId));
    }

    @GetMapping("/by-task/{taskId}")
    @Operation(summary = "按质检任务查询不合格品列表")
    public ResponseEntity<java.util.List<NonConformingProductDTO>> listByTaskId(@PathVariable Long taskId) {
        return ResponseEntity.ok(nonConformingProductApplicationService.listByTaskId(taskId));
    }

    @PostMapping("/{id}/dispose")
    @Operation(summary = "处置完成")
    public ResponseEntity<NonConformingProductDTO> dispose(
            @PathVariable Long id,
            @RequestBody(required = false) com.ly.factmesh.qms.application.dto.NcrDisposeRequest request
    ) {
        return ResponseEntity.ok(nonConformingProductApplicationService.dispose(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除不合格品记录")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        nonConformingProductApplicationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
