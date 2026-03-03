package com.ly.factmesh.wms.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.wms.application.dto.InventoryAdjustRequest;
import com.ly.factmesh.wms.application.dto.InventoryCountRequest;
import com.ly.factmesh.wms.application.dto.InventoryDTO;
import com.ly.factmesh.wms.application.dto.InventoryTransactionDTO;
import com.ly.factmesh.common.enums.InventoryTransactionTypeEnum;
import com.ly.factmesh.wms.domain.entity.Inventory;
import com.ly.factmesh.wms.domain.entity.InventoryTransaction;
import com.ly.factmesh.wms.domain.entity.Material;
import com.ly.factmesh.wms.domain.repository.InventoryRepository;
import com.ly.factmesh.wms.domain.repository.InventoryTransactionRepository;
import com.ly.factmesh.wms.domain.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 库存应用服务
 *
 * @author LY-FactMesh
 */
@Service
@RequiredArgsConstructor
public class InventoryApplicationService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final MaterialRepository materialRepository;

    public InventoryDTO getById(Long id) {
        Inventory inv = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("库存记录不存在: " + id));
        return toDTO(inv);
    }

    public List<InventoryDTO> findByMaterialId(Long materialId) {
        return inventoryRepository.findByMaterialId(materialId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * 获取物料可用库存总量（所有仓库汇总）
     */
    public int getTotalQuantityByMaterialId(Long materialId) {
        return inventoryRepository.findByMaterialId(materialId).stream()
                .mapToInt(inv -> inv.getQuantity() != null ? inv.getQuantity() : 0)
                .sum();
    }

    public Page<InventoryDTO> page(int pageNum, int pageSize, Long materialId, String warehouse, String batchNo) {
        long offset = (long) (pageNum - 1) * pageSize;
        long total = inventoryRepository.count(materialId, warehouse, batchNo);
        List<Inventory> list = inventoryRepository.findAll(offset, pageSize, materialId, warehouse, batchNo);
        List<InventoryDTO> records = list.stream().map(this::toDTO).collect(Collectors.toList());
        Page<InventoryDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    /**
     * 调整库存（入库/出库），并记录事务
     */
    @Transactional(rollbackFor = Exception.class)
    public void adjust(InventoryAdjustRequest request) {
        if (request.getQuantity() == null || request.getQuantity() == 0) {
            throw new IllegalArgumentException("调整数量不能为0");
        }
        String warehouse = request.getWarehouse() != null ? request.getWarehouse() : "";
        String location = request.getLocation() != null ? request.getLocation() : "";
        String batchNo = (request.getBatchNo() != null && !request.getBatchNo().isBlank()) ? request.getBatchNo() : null;

        Inventory inv = inventoryRepository.findByMaterialAndLocation(request.getMaterialId(), warehouse, location, batchNo)
                .orElseGet(() -> {
                    Inventory i = new Inventory();
                    i.setMaterialId(request.getMaterialId());
                    i.setBatchNo(batchNo);
                    i.setWarehouse(warehouse);
                    i.setLocation(location);
                    i.setQuantity(0);
                    i.setSafeStock(0);
                    return inventoryRepository.save(i);
                });

        int newQty = (inv.getQuantity() != null ? inv.getQuantity() : 0) + request.getQuantity();
        if (newQty < 0) {
            throw new IllegalArgumentException("库存不足，当前: " + inv.getQuantity() + ", 需扣减: " + (-request.getQuantity()));
        }
        inv.setQuantity(newQty);
        inv.setLastUpdateTime(LocalDateTime.now());
        inventoryRepository.save(inv);

        int txType = request.getQuantity() > 0 ? InventoryTransactionTypeEnum.INBOUND.getCode() : InventoryTransactionTypeEnum.OUTBOUND.getCode();
        InventoryTransaction tx = new InventoryTransaction();
        tx.setMaterialId(request.getMaterialId());
        tx.setBatchNo(batchNo);
        tx.setTransactionType(txType);
        tx.setQuantity(Math.abs(request.getQuantity()));
        tx.setWarehouse(warehouse);
        tx.setLocation(location);
        tx.setOrderId(request.getOrderId());
        tx.setReqId(request.getReqId());
        tx.setTransactionTime(LocalDateTime.now());
        tx.setOperator(request.getOperator());
        tx.setReferenceNo(request.getReferenceNo());
        transactionRepository.save(tx);
    }

    /**
     * 盘点确认：录入实盘数量，系统自动计算差异并调整库存，记录盘点调整事务
     */
    @Transactional(rollbackFor = Exception.class)
    public void count(InventoryCountRequest request) {
        if (request.getActualQuantity() == null || request.getActualQuantity() < 0) {
            throw new IllegalArgumentException("实盘数量不能为负数");
        }
        Inventory inv = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new IllegalArgumentException("库存记录不存在: " + request.getInventoryId()));
        int bookQty = inv.getQuantity() != null ? inv.getQuantity() : 0;
        int actualQty = request.getActualQuantity();
        int diff = actualQty - bookQty;
        if (diff == 0) {
            return; // 账实相符，无需调整
        }
        inv.setQuantity(actualQty);
        inv.setLastUpdateTime(LocalDateTime.now());
        inventoryRepository.save(inv);

        InventoryTransaction tx = new InventoryTransaction();
        tx.setMaterialId(inv.getMaterialId());
        tx.setBatchNo(inv.getBatchNo());
        tx.setTransactionType(InventoryTransactionTypeEnum.ADJUST.getCode());
        tx.setQuantity(Math.abs(diff));
        tx.setWarehouse(inv.getWarehouse());
        tx.setLocation(inv.getLocation());
        tx.setTransactionTime(LocalDateTime.now());
        tx.setOperator(request.getOperator() != null ? request.getOperator() : "system");
        tx.setReferenceNo("COUNT-" + inv.getId());
        transactionRepository.save(tx);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSafeStock(Long inventoryId, Integer safeStock) {
        if (safeStock != null && safeStock < 0) {
            throw new IllegalArgumentException("安全库存不能为负数");
        }
        Inventory inv = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("库存记录不存在: " + inventoryId));
        inv.setSafeStock(safeStock != null ? safeStock : 0);
        inv.setLastUpdateTime(LocalDateTime.now());
        inventoryRepository.save(inv);
    }

    public Page<InventoryDTO> pageBelowSafeStock(int pageNum, int pageSize) {
        long offset = (long) (pageNum - 1) * pageSize;
        long total = inventoryRepository.countBelowSafeStock();
        List<Inventory> list = inventoryRepository.findAllBelowSafeStock(offset, pageSize);
        List<InventoryDTO> records = list.stream().map(this::toDTO).collect(Collectors.toList());
        Page<InventoryDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    /**
     * 内部方法：领料出库（由领料完成流程调用），记录 orderId/reqId 用于追溯
     */
    @Transactional(rollbackFor = Exception.class)
    public void deductForRequisition(Long materialId, int quantity, String referenceNo, Long reqId, Long orderId) {
        InventoryAdjustRequest req = new InventoryAdjustRequest();
        req.setMaterialId(materialId);
        req.setQuantity(-quantity);
        req.setReferenceNo(referenceNo);
        req.setOperator("system");
        req.setReqId(reqId);
        req.setOrderId(orderId);
        adjust(req);
    }

    /**
     * 内部方法：退料入库（由退料流程调用），记录 orderId/reqId 用于追溯
     */
    @Transactional(rollbackFor = Exception.class)
    public void addForReturn(Long materialId, int quantity, String referenceNo, Long reqId, Long orderId) {
        InventoryAdjustRequest req = new InventoryAdjustRequest();
        req.setMaterialId(materialId);
        req.setQuantity(quantity);
        req.setReferenceNo(referenceNo);
        req.setOperator("system");
        req.setReqId(reqId);
        req.setOrderId(orderId);
        adjust(req);
    }

    public Page<InventoryTransactionDTO> getTransactions(Long materialId, int pageNum, int pageSize) {
        long offset = (long) (pageNum - 1) * pageSize;
        long total = transactionRepository.countByMaterialId(materialId);
        List<InventoryTransactionDTO> records = transactionRepository.findByMaterialId(materialId, offset, pageSize).stream()
                .map(this::toTransactionDTO).collect(Collectors.toList());
        Page<InventoryTransactionDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    /**
     * 物料追溯：按物料、批次、工单、领料单查询出入库记录
     */
    public Page<InventoryTransactionDTO> trace(Long materialId, String batchNo, Long orderId, Long reqId, int pageNum, int pageSize) {
        long offset = (long) (pageNum - 1) * pageSize;
        long total = transactionRepository.countByTraceCriteria(materialId, batchNo, orderId, reqId);
        List<InventoryTransactionDTO> records = transactionRepository.findByTraceCriteria(materialId, batchNo, orderId, reqId, offset, pageSize).stream()
                .map(this::toTransactionDTO).collect(Collectors.toList());
        Page<InventoryTransactionDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    private InventoryDTO toDTO(Inventory inv) {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inv.getId());
        dto.setMaterialId(inv.getMaterialId());
        dto.setWarehouse(inv.getWarehouse());
        dto.setLocation(inv.getLocation());
        dto.setQuantity(inv.getQuantity());
        dto.setSafeStock(inv.getSafeStock());
        dto.setBelowSafeStock(inv.getSafeStock() != null && inv.getSafeStock() > 0
                && (inv.getQuantity() == null || inv.getQuantity() < inv.getSafeStock()));
        dto.setLastUpdateTime(inv.getLastUpdateTime());
        materialRepository.findById(inv.getMaterialId()).ifPresent(m -> {
            dto.setMaterialCode(m.getMaterialCode());
            dto.setMaterialName(m.getMaterialName());
        });
        return dto;
    }

    private InventoryTransactionDTO toTransactionDTO(InventoryTransaction tx) {
        InventoryTransactionDTO dto = new InventoryTransactionDTO();
        dto.setId(tx.getId());
        dto.setMaterialId(tx.getMaterialId());
        dto.setBatchNo(tx.getBatchNo());
        dto.setTransactionType(tx.getTransactionType());
        dto.setQuantity(tx.getQuantity());
        dto.setWarehouse(tx.getWarehouse());
        dto.setLocation(tx.getLocation());
        dto.setOrderId(tx.getOrderId());
        dto.setReqId(tx.getReqId());
        dto.setTransactionTime(tx.getTransactionTime());
        dto.setOperator(tx.getOperator());
        dto.setReferenceNo(tx.getReferenceNo());
        materialRepository.findById(tx.getMaterialId()).ifPresent(m -> {
            dto.setMaterialCode(m.getMaterialCode());
            dto.setMaterialName(m.getMaterialName());
        });
        return dto;
    }
}
