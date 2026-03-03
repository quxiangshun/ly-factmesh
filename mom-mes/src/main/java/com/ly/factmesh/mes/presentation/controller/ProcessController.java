package com.ly.factmesh.mes.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.mes.application.dto.ProcessCreateRequest;
import com.ly.factmesh.mes.application.dto.ProcessDTO;
import com.ly.factmesh.mes.application.dto.ProcessUpdateRequest;
import com.ly.factmesh.mes.application.service.ProcessApplicationService;
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
 * 工序 REST 控制器
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/processes")
@RequiredArgsConstructor
@Tag(name = "工序管理", description = "工序定义 CRUD")
public class ProcessController {

    private final ProcessApplicationService processApplicationService;

    @PostMapping
    @Operation(summary = "创建工序")
    public ResponseEntity<ProcessDTO> create(@Valid @RequestBody ProcessCreateRequest request) {
        ProcessDTO dto = processApplicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询工序")
    public ResponseEntity<ProcessDTO> getById(@PathVariable @Parameter(description = "工序ID") Long id) {
        return ResponseEntity.ok(processApplicationService.getById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询工序")
    public ResponseEntity<Page<ProcessDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size
    ) {
        return ResponseEntity.ok(processApplicationService.page(page, size));
    }

    @GetMapping("/list")
    @Operation(summary = "获取全部工序列表（用于下拉选择）")
    public ResponseEntity<List<ProcessDTO>> listAll() {
        return ResponseEntity.ok(processApplicationService.listAll());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新工序")
    public ResponseEntity<ProcessDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProcessUpdateRequest request
    ) {
        return ResponseEntity.ok(processApplicationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除工序")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        processApplicationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
