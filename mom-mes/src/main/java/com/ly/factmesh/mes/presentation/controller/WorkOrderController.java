package com.ly.factmesh.mes.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.mes.application.dto.WorkOrderCreateRequest;
import com.ly.factmesh.mes.application.dto.WorkOrderDTO;
import com.ly.factmesh.mes.application.dto.WorkOrderStatsDTO;
import com.ly.factmesh.mes.application.dto.WorkOrderSummaryDTO;
import com.ly.factmesh.mes.application.service.WorkOrderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    @GetMapping("/summary")
    @Operation(summary = "生产汇总", description = "按日统计完成工单数、产量及进行中/暂停数量")
    public ResponseEntity<WorkOrderSummaryDTO> getSummary(
            @RequestParam(required = false) @Parameter(description = "统计日期，默认当天") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(workOrderApplicationService.getSummary(date));
    }

    @GetMapping("/summary/detail")
    @Operation(summary = "生产日报明细", description = "按日查询已完成工单列表")
    public ResponseEntity<java.util.List<WorkOrderDTO>> getSummaryDetail(
            @RequestParam(required = false) @Parameter(description = "统计日期，默认当天") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") Integer size
    ) {
        return ResponseEntity.ok(workOrderApplicationService.getCompletedByDate(date, page, size));
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

    @PostMapping("/{id}/pause")
    @Operation(summary = "暂停工单")
    public ResponseEntity<WorkOrderDTO> pause(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderApplicationService.pause(id));
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "恢复工单")
    public ResponseEntity<WorkOrderDTO> resume(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderApplicationService.resume(id));
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
