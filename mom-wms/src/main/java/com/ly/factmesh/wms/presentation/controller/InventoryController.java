package com.ly.factmesh.wms.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.wms.application.dto.InventoryAdjustRequest;
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

    @GetMapping
    @Operation(summary = "分页查询库存")
    public ResponseEntity<Page<InventoryDTO>> page(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size,
            @RequestParam(required = false) @Parameter(description = "物料ID") Long materialId,
            @RequestParam(required = false) @Parameter(description = "仓库") String warehouse
    ) {
        return ResponseEntity.ok(inventoryApplicationService.page(page, size, materialId, warehouse));
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
}
