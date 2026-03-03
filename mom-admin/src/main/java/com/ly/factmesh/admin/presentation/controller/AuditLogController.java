package com.ly.factmesh.admin.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.application.dto.AuditLogCreateRequest;
import com.ly.factmesh.admin.application.dto.AuditLogDTO;
import com.ly.factmesh.admin.application.service.AuditLogApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 审计日志 REST 控制器
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "审计日志", description = "数据变更记录")
public class AuditLogController {

    private final AuditLogApplicationService auditLogApplicationService;

    @PostMapping
    @Operation(summary = "记录审计日志", description = "数据变更时调用")
    public ResponseEntity<Void> create(@Valid @RequestBody AuditLogCreateRequest request) {
        auditLogApplicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @Operation(summary = "分页查询审计日志")
    public ResponseEntity<Page<AuditLogDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") Integer size,
            @RequestParam(required = false) @Parameter(description = "表名") String tableName,
            @RequestParam(required = false) @Parameter(description = "记录ID") String recordId,
            @RequestParam(required = false) @Parameter(description = "操作类型") String operationType
    ) {
        return ResponseEntity.ok(auditLogApplicationService.page(page, size, tableName, recordId, operationType));
    }
}
