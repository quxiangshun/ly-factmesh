package com.ly.factmesh.wms.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.wms.domain.entity.InventoryTransaction;
import com.ly.factmesh.wms.domain.repository.InventoryTransactionRepository;
import com.ly.factmesh.wms.infrastructure.database.entity.InventoryTransactionEntity;
import com.ly.factmesh.wms.infrastructure.database.mapper.InventoryTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InventoryTransactionRepositoryImpl implements InventoryTransactionRepository {

    private final InventoryTransactionMapper transactionMapper;

    @Override
    public InventoryTransaction save(InventoryTransaction transaction) {
        InventoryTransactionEntity e = toEntity(transaction);
        if (e.getId() == null) {
            e.setId(SnowflakeIdGenerator.generateId());
            if (e.getTransactionTime() == null) e.setTransactionTime(LocalDateTime.now());
            transactionMapper.insert(e);
        } else {
            transactionMapper.updateById(e);
        }
        return toDomain(e);
    }

    @Override
    public List<InventoryTransaction> findByMaterialId(Long materialId, long offset, long limit) {
        if (materialId == null) return List.of();
        long pageNum = limit <= 0 ? 1 : offset / limit + 1;
        Page<InventoryTransactionEntity> page = new Page<>(pageNum, limit);
        LambdaQueryWrapper<InventoryTransactionEntity> q = new LambdaQueryWrapper<>();
        q.eq(InventoryTransactionEntity::getMaterialId, materialId);
        q.orderByDesc(InventoryTransactionEntity::getTransactionTime);
        return transactionMapper.selectPage(page, q).getRecords().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countByMaterialId(Long materialId) {
        if (materialId == null) return 0;
        LambdaQueryWrapper<InventoryTransactionEntity> q = new LambdaQueryWrapper<>();
        q.eq(InventoryTransactionEntity::getMaterialId, materialId);
        return transactionMapper.selectCount(q);
    }

    @Override
    public List<InventoryTransaction> findByTraceCriteria(Long materialId, String batchNo, Long orderId, Long reqId, long offset, long limit) {
        long pageNum = limit <= 0 ? 1 : offset / limit + 1;
        Page<InventoryTransactionEntity> page = new Page<>(pageNum, limit);
        LambdaQueryWrapper<InventoryTransactionEntity> q = buildTraceQuery(materialId, batchNo, orderId, reqId);
        q.orderByDesc(InventoryTransactionEntity::getTransactionTime);
        return transactionMapper.selectPage(page, q).getRecords().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countByTraceCriteria(Long materialId, String batchNo, Long orderId, Long reqId) {
        LambdaQueryWrapper<InventoryTransactionEntity> q = buildTraceQuery(materialId, batchNo, orderId, reqId);
        return transactionMapper.selectCount(q);
    }

    private LambdaQueryWrapper<InventoryTransactionEntity> buildTraceQuery(Long materialId, String batchNo, Long orderId, Long reqId) {
        LambdaQueryWrapper<InventoryTransactionEntity> q = new LambdaQueryWrapper<>();
        if (materialId != null) q.eq(InventoryTransactionEntity::getMaterialId, materialId);
        if (batchNo != null && !batchNo.isBlank()) q.eq(InventoryTransactionEntity::getBatchNo, batchNo);
        if (orderId != null) q.eq(InventoryTransactionEntity::getOrderId, orderId);
        if (reqId != null) q.eq(InventoryTransactionEntity::getReqId, reqId);
        return q;
    }

    private InventoryTransactionEntity toEntity(InventoryTransaction d) {
        InventoryTransactionEntity e = new InventoryTransactionEntity();
        e.setId(d.getId());
        e.setMaterialId(d.getMaterialId());
        e.setBatchNo(d.getBatchNo());
        e.setTransactionType(d.getTransactionType());
        e.setQuantity(d.getQuantity());
        e.setWarehouse(d.getWarehouse());
        e.setLocation(d.getLocation());
        e.setOrderId(d.getOrderId());
        e.setReqId(d.getReqId());
        e.setTransactionTime(d.getTransactionTime());
        e.setOperator(d.getOperator());
        e.setReferenceNo(d.getReferenceNo());
        return e;
    }

    private InventoryTransaction toDomain(InventoryTransactionEntity e) {
        InventoryTransaction d = new InventoryTransaction();
        d.setId(e.getId());
        d.setMaterialId(e.getMaterialId());
        d.setBatchNo(e.getBatchNo());
        d.setTransactionType(e.getTransactionType());
        d.setQuantity(e.getQuantity());
        d.setWarehouse(e.getWarehouse());
        d.setLocation(e.getLocation());
        d.setOrderId(e.getOrderId());
        d.setReqId(e.getReqId());
        d.setTransactionTime(e.getTransactionTime());
        d.setOperator(e.getOperator());
        d.setReferenceNo(e.getReferenceNo());
        return d;
    }
}
