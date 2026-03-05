package com.ly.factmesh.wms.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.wms.application.dto.InventoryAdjustRequest;
import com.ly.factmesh.wms.application.dto.InventoryCountRequest;
import com.ly.factmesh.wms.application.dto.InventoryDTO;
import com.ly.factmesh.wms.application.dto.InventoryTransactionDTO;
import com.ly.factmesh.wms.application.service.InventoryApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存 REST 控制器
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "库存管理", description = "库存查询、调整、出入库记录")
public class InventoryController {

    private final InventoryApplicationService inventoryApplicationService;

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询库存")
    public ResponseEntity<InventoryDTO> getById(@PathVariable @Parameter(description = "库存ID") Long id) {
        return ResponseEntity.ok(inventoryApplicationService.getById(id));
    }

    @GetMapping("/material/{materialId}")
    @Operation(summary = "根据物料ID查询库存列表")
    public ResponseEntity<List<InventoryDTO>> findByMaterialId(@PathVariable @Parameter(description = "物料ID") Long materialId) {
        return ResponseEntity.ok(inventoryApplicationService.findByMaterialId(materialId));
    }

    @GetMapping("/stats")
    @Operation(summary = "库存统计", description = "用于报表：总记录数、低于安全库存数")
    public ResponseEntity<java.util.Map<String, Object>> getStats() {
        return ResponseEntity.ok(inventoryApplicationService.getStats());
    }

    @GetMapping
    @Operation(summary = "分页查询库存")
    public ResponseEntity<Page<InventoryDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size,
            @RequestParam(required = false) @Parameter(description = "物料ID") Long materialId,
            @RequestParam(required = false) @Parameter(description = "仓库") String warehouse,
            @RequestParam(required = false) @Parameter(description = "批次号") String batchNo
    ) {
        return ResponseEntity.ok(inventoryApplicationService.page(page, size, materialId, warehouse, batchNo));
    }

    @GetMapping("/below-safe-stock")
    @Operation(summary = "分页查询低于安全库存的库存")
    public ResponseEntity<Page<InventoryDTO>> pageBelowSafeStock(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size
    ) {
        return ResponseEntity.ok(inventoryApplicationService.pageBelowSafeStock(page, size));
    }

    @PostMapping("/adjust")
    @Operation(summary = "调整库存", description = "正数入库、负数出库")
    public ResponseEntity<Void> adjust(@Valid @RequestBody InventoryAdjustRequest request) {
        inventoryApplicationService.adjust(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/count")
    @Operation(summary = "盘点确认", description = "录入实盘数量，系统自动计算差异并调整库存")
    public ResponseEntity<Void> count(@Valid @RequestBody InventoryCountRequest request) {
        inventoryApplicationService.count(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}/safe-stock")
    @Operation(summary = "更新安全库存")
    public ResponseEntity<Void> updateSafeStock(
            @PathVariable @Parameter(description = "库存ID") Long id,
            @RequestParam @Parameter(description = "安全库存数量") Integer safeStock
    ) {
        inventoryApplicationService.updateSafeStock(id, safeStock);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/transactions")
    @Operation(summary = "分页查询物料出入库记录")
    public ResponseEntity<Page<InventoryTransactionDTO>> getTransactions(
            @RequestParam @Parameter(description = "物料ID") Long materialId,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") Integer size
    ) {
        return ResponseEntity.ok(inventoryApplicationService.getTransactions(materialId, page, size));
    }

    @GetMapping("/trace")
    @Operation(summary = "物料追溯", description = "按物料、批次、工单、领料单查询出入库记录，支持多条件组合")
    public ResponseEntity<Page<InventoryTransactionDTO>> trace(
            @RequestParam(required = false) @Parameter(description = "物料ID") Long materialId,
            @RequestParam(required = false) @Parameter(description = "批次号") String batchNo,
            @RequestParam(required = false) @Parameter(description = "工单ID") Long orderId,
            @RequestParam(required = false) @Parameter(description = "领料单ID") Long reqId,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") Integer size
    ) {
        return ResponseEntity.ok(inventoryApplicationService.trace(materialId, batchNo, orderId, reqId, page, size));
    }
}
