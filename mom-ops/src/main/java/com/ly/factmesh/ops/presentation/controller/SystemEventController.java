package com.ly.factmesh.ops.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.ops.application.dto.SystemEventDTO;
import com.ly.factmesh.ops.application.service.SystemEventApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system-events")
@RequiredArgsConstructor
@Tag(name = "系统事件", description = "系统级事件")
public class SystemEventController {

    private final SystemEventApplicationService systemEventApplicationService;

    @GetMapping
    @Operation(summary = "分页查询系统事件")
    public ResponseEntity<Page<SystemEventDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") Integer size,
            @RequestParam(required = false) @Parameter(description = "事件类型") String eventType,
            @RequestParam(required = false) @Parameter(description = "已处理：0-未处理 1-已处理") Integer processed
    ) {
        return ResponseEntity.ok(systemEventApplicationService.page(page, size, eventType, processed));
    }
}
