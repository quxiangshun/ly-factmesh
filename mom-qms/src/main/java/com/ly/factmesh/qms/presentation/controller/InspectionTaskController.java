package com.ly.factmesh.qms.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.qms.application.dto.InspectionTaskCreateRequest;
import com.ly.factmesh.qms.application.dto.InspectionTaskDTO;
import com.ly.factmesh.qms.application.service.InspectionTaskApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 质检任务 REST 控制器
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/inspection-tasks")
@RequiredArgsConstructor
@Tag(name = "质检任务管理", description = "质检任务创建、查询、分页、完成、删除")
public class InspectionTaskController {

    private final InspectionTaskApplicationService inspectionTaskApplicationService;

    @PostMapping
    @Operation(summary = "创建质检任务")
    public ResponseEntity<InspectionTaskDTO> create(@Valid @RequestBody InspectionTaskCreateRequest request) {
        InspectionTaskDTO dto = inspectionTaskApplicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询质检任务")
    public ResponseEntity<InspectionTaskDTO> getById(@PathVariable @Parameter(description = "任务ID") Long id) {
        return ResponseEntity.ok(inspectionTaskApplicationService.getById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询质检任务")
    public ResponseEntity<Page<InspectionTaskDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size
    ) {
        return ResponseEntity.ok(inspectionTaskApplicationService.page(page, size));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "完成质检任务")
    public ResponseEntity<InspectionTaskDTO> complete(@PathVariable Long id) {
        return ResponseEntity.ok(inspectionTaskApplicationService.complete(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除质检任务")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inspectionTaskApplicationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
