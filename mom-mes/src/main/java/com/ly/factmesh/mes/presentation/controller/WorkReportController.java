package com.ly.factmesh.mes.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.mes.application.dto.WorkReportCreateRequest;
import com.ly.factmesh.mes.application.dto.WorkReportDTO;
import com.ly.factmesh.mes.application.service.WorkReportApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 报工 REST 控制器
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/work-reports")
@RequiredArgsConstructor
@Tag(name = "报工管理", description = "报工录入、查询")
public class WorkReportController {

    private final WorkReportApplicationService workReportApplicationService;

    @PostMapping
    @Operation(summary = "创建报工")
    public ResponseEntity<WorkReportDTO> create(@Valid @RequestBody WorkReportCreateRequest request) {
        WorkReportDTO dto = workReportApplicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询报工")
    public ResponseEntity<WorkReportDTO> getById(@PathVariable @Parameter(description = "报工ID") Long id) {
        return ResponseEntity.ok(workReportApplicationService.getById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询报工")
    public ResponseEntity<Page<WorkReportDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size,
            @RequestParam(required = false) @Parameter(description = "工单ID筛选") Long orderId
    ) {
        if (orderId != null) {
            return ResponseEntity.ok(workReportApplicationService.pageByOrderId(orderId, page, size));
        }
        return ResponseEntity.ok(workReportApplicationService.page(page, size));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除报工")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workReportApplicationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
