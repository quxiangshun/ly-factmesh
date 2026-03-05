package com.ly.factmesh.admin.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.application.dto.ReportDefDTO;
import com.ly.factmesh.admin.application.service.ReportApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 报表控制器：预定义模板、自定义报表 CRUD、执行
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "报表管理", description = "报表模板、自定义报表、执行")
public class ReportController {

    private final ReportApplicationService reportApplicationService;

    @GetMapping("/templates")
    @Operation(summary = "预定义报表模板列表")
    public ResponseEntity<List<Map<String, Object>>> listTemplates() {
        return ResponseEntity.ok(reportApplicationService.listTemplates());
    }

    @GetMapping("/definitions")
    @Operation(summary = "分页查询自定义报表定义")
    public ResponseEntity<Page<ReportDefDTO>> listDefinitions(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") Integer size
    ) {
        return ResponseEntity.ok(reportApplicationService.listDefinitions(page, size));
    }

    @GetMapping("/definitions/{id}")
    @Operation(summary = "根据ID查询报表定义")
    public ResponseEntity<ReportDefDTO> getDefinitionById(@PathVariable Long id) {
        return ResponseEntity.ok(reportApplicationService.getDefinitionById(id));
    }

    @PostMapping("/definitions")
    @Operation(summary = "创建自定义报表")
    public ResponseEntity<ReportDefDTO> createDefinition(@Valid @RequestBody ReportDefDTO request) {
        ReportDefDTO saved = reportApplicationService.createDefinition(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/definitions/{id}")
    @Operation(summary = "更新自定义报表")
    public ResponseEntity<ReportDefDTO> updateDefinition(@PathVariable Long id, @Valid @RequestBody ReportDefDTO request) {
        return ResponseEntity.ok(reportApplicationService.updateDefinition(id, request));
    }

    @DeleteMapping("/definitions/{id}")
    @Operation(summary = "删除自定义报表")
    public ResponseEntity<Void> deleteDefinition(@PathVariable Long id) {
        reportApplicationService.deleteDefinition(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/execute")
    @Operation(summary = "执行报表", description = "按模板类型或定义ID执行，返回表格数据")
    public ResponseEntity<Map<String, Object>> execute(
            @RequestBody Map<String, Object> request
    ) {
        return ResponseEntity.ok(reportApplicationService.execute(request));
    }
}
