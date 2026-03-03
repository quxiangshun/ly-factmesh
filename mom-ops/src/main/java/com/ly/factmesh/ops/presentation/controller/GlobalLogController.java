package com.ly.factmesh.ops.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.ops.application.dto.GlobalLogCreateRequest;
import com.ly.factmesh.ops.application.dto.GlobalLogDTO;
import com.ly.factmesh.ops.application.service.GlobalLogApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/global-logs")
@RequiredArgsConstructor
@Tag(name = "全局日志", description = "跨服务全局日志")
public class GlobalLogController {

    private final GlobalLogApplicationService globalLogApplicationService;

    @PostMapping
    @Operation(summary = "记录全局日志")
    public ResponseEntity<Void> create(@Valid @RequestBody GlobalLogCreateRequest request) {
        globalLogApplicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @Operation(summary = "分页查询全局日志")
    public ResponseEntity<Page<GlobalLogDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") Integer size,
            @RequestParam(required = false) @Parameter(description = "服务名") String serviceName
    ) {
        return ResponseEntity.ok(globalLogApplicationService.page(page, size, serviceName));
    }
}
