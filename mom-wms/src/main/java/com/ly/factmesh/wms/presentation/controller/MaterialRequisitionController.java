package com.ly.factmesh.wms.presentation.controller;

import com.ly.factmesh.common.feign.dto.RequisitionCreateRequest;
import com.ly.factmesh.wms.application.service.MaterialRequisitionApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 领料单 REST 控制器（供 MES 工单发布时 Feign 调用）
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/requisitions")
@RequiredArgsConstructor
@Tag(name = "领料单", description = "领料单创建（MES 工单发布触发）")
public class MaterialRequisitionController {

    private final MaterialRequisitionApplicationService requisitionApplicationService;

    @PostMapping
    @Operation(summary = "创建领料单", description = "由 MES 工单发布时调用，创建领料单及明细")
    public ResponseEntity<Long> create(@Valid @RequestBody RequisitionCreateRequest request) {
        Long id = requisitionApplicationService.createFromRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }
}
