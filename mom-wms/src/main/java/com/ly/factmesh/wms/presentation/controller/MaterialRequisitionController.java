package com.ly.factmesh.wms.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.feign.dto.RequisitionCreateRequest;
import com.ly.factmesh.wms.application.dto.MaterialRequisitionDTO;
import com.ly.factmesh.wms.application.dto.RequisitionCompleteRequest;
import com.ly.factmesh.wms.application.dto.RequisitionManualCreateRequest;
import com.ly.factmesh.wms.application.service.MaterialRequisitionApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "领料单", description = "领料单创建、查询、状态流转")
public class MaterialRequisitionController {

    private final MaterialRequisitionApplicationService requisitionApplicationService;

    @PostMapping
    @Operation(summary = "创建领料单", description = "由 MES 工单发布时调用，创建领料单及明细")
    public ResponseEntity<Long> create(@Valid @RequestBody RequisitionCreateRequest request) {
        Long id = requisitionApplicationService.createFromRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PostMapping("/draft")
    @Operation(summary = "手动创建领料单（草稿）")
    public ResponseEntity<Long> createDraft(@Valid @RequestBody RequisitionManualCreateRequest request) {
        Long id = requisitionApplicationService.createDraft(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消领料单")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        requisitionApplicationService.cancel(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询领料单详情")
    public ResponseEntity<MaterialRequisitionDTO> getById(@PathVariable @Parameter(description = "领料单ID") Long id) {
        return ResponseEntity.ok(requisitionApplicationService.getById(id));
    }

    @GetMapping("/no/{reqNo}")
    @Operation(summary = "根据领料单号查询详情")
    public ResponseEntity<MaterialRequisitionDTO> getByReqNo(@PathVariable @Parameter(description = "领料单号") String reqNo) {
        return ResponseEntity.ok(requisitionApplicationService.getByReqNo(reqNo));
    }

    @GetMapping
    @Operation(summary = "分页查询领料单")
    public ResponseEntity<Page<MaterialRequisitionDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size,
            @RequestParam(required = false) @Parameter(description = "工单ID") Long orderId,
            @RequestParam(required = false) @Parameter(description = "状态：0-草稿 1-已提交 2-已完成") Integer status
    ) {
        return ResponseEntity.ok(requisitionApplicationService.page(page, size, orderId, status));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "提交领料单", description = "草稿→已提交")
    public ResponseEntity<Void> submit(@PathVariable Long id) {
        requisitionApplicationService.submit(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "完成领料单", description = "更新实发数量并完成")
    public ResponseEntity<Void> complete(@PathVariable Long id,
                                         @RequestBody(required = false) RequisitionCompleteRequest request) {
        requisitionApplicationService.complete(id, request);
        return ResponseEntity.ok().build();
    }
}
