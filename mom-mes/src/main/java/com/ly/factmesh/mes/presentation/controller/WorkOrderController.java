package com.ly.factmesh.mes.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.mes.application.dto.WorkOrderCreateRequest;
import com.ly.factmesh.mes.application.dto.WorkOrderDTO;
import com.ly.factmesh.mes.application.dto.WorkOrderStatsDTO;
import com.ly.factmesh.mes.application.service.WorkOrderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 工单 REST 控制器
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
@Tag(name = "工单管理", description = "工单创建、下发、开始、完成")
public class WorkOrderController {

    private final WorkOrderApplicationService workOrderApplicationService;

    @PostMapping
    @Operation(summary = "创建工单", description = "创建草稿状态工单")
    public ResponseEntity<WorkOrderDTO> create(@Valid @RequestBody WorkOrderCreateRequest request) {
        WorkOrderDTO dto = workOrderApplicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询工单")
    public ResponseEntity<WorkOrderDTO> getById(@PathVariable @Parameter(description = "工单ID") Long id) {
        return ResponseEntity.ok(workOrderApplicationService.getById(id));
    }

    @GetMapping("/stats")
    @Operation(summary = "工单统计")
    public ResponseEntity<WorkOrderStatsDTO> getStats() {
        return ResponseEntity.ok(workOrderApplicationService.getStats());
    }

    @GetMapping
    @Operation(summary = "分页查询工单")
    public ResponseEntity<Page<WorkOrderDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size
    ) {
        return ResponseEntity.ok(workOrderApplicationService.page(page, size));
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "下发工单")
    public ResponseEntity<WorkOrderDTO> release(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderApplicationService.release(id));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "开始工单")
    public ResponseEntity<WorkOrderDTO> start(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderApplicationService.start(id));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "完成工单", description = "可传入实际数量")
    public ResponseEntity<WorkOrderDTO> complete(
            @PathVariable Long id,
            @RequestParam(required = false) Integer actualQuantity
    ) {
        return ResponseEntity.ok(workOrderApplicationService.complete(id, actualQuantity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除工单")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workOrderApplicationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
