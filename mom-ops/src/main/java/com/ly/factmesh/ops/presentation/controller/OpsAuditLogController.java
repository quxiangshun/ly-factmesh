package com.ly.factmesh.ops.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.ops.application.dto.OpsAuditLogDTO;
import com.ly.factmesh.ops.application.service.OpsAuditLogApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ops-audit-logs")
@RequiredArgsConstructor
@Tag(name = "运维审计", description = "跨服务审计记录")
public class OpsAuditLogController {

    private final OpsAuditLogApplicationService opsAuditLogApplicationService;

    @GetMapping
    @Operation(summary = "分页查询运维审计日志")
    public ResponseEntity<Page<OpsAuditLogDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") Integer size,
            @RequestParam(required = false) @Parameter(description = "服务名") String serviceName,
            @RequestParam(required = false) @Parameter(description = "用户ID") Long userId
    ) {
        return ResponseEntity.ok(opsAuditLogApplicationService.page(page, size, serviceName, userId));
    }
}
