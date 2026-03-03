package com.ly.factmesh.admin.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.application.dto.OperationLogCreateRequest;
import com.ly.factmesh.admin.application.dto.OperationLogDTO;
import com.ly.factmesh.admin.application.service.OperationLogApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operation-logs")
@RequiredArgsConstructor
@Tag(name = "操作日志", description = "谁在何时执行了哪些操作")
public class OperationLogController {

    private final OperationLogApplicationService operationLogApplicationService;

    @PostMapping
    @Operation(summary = "记录操作日志", description = "供网关/拦截器调用")
    public ResponseEntity<Void> create(@RequestBody OperationLogCreateRequest request) {
        operationLogApplicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @Operation(summary = "分页查询操作日志")
    public ResponseEntity<Page<OperationLogDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") Integer size,
            @RequestParam(required = false) @Parameter(description = "用户ID") Long userId,
            @RequestParam(required = false) @Parameter(description = "模块") String module,
            @RequestParam(required = false) @Parameter(description = "用户名") String username
    ) {
        return ResponseEntity.ok(operationLogApplicationService.page(page, size, userId, module, username));
    }
}
