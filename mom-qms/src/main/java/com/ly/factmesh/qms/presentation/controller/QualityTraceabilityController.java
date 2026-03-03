package com.ly.factmesh.qms.presentation.controller;

import com.ly.factmesh.qms.application.dto.QualityTraceabilityDTO;
import com.ly.factmesh.qms.application.service.QualityTraceabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 质量追溯 REST 控制器（关联工单/设备/物料）
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/quality-trace")
@RequiredArgsConstructor
@Tag(name = "质量追溯", description = "按产品/批次/工单查询质量追溯记录")
public class QualityTraceabilityController {

    private final QualityTraceabilityService qualityTraceabilityService;

    @GetMapping
    @Operation(summary = "质量追溯查询", description = "按产品编码、批次号、工单编码多条件查询")
    public ResponseEntity<List<QualityTraceabilityDTO>> trace(
            @RequestParam(required = false) @Parameter(description = "产品编码") String productCode,
            @RequestParam(required = false) @Parameter(description = "批次号") String batchNo,
            @RequestParam(required = false) @Parameter(description = "工单编码") String productionOrder
    ) {
        return ResponseEntity.ok(qualityTraceabilityService.trace(productCode, batchNo, productionOrder));
    }
}
