package com.ly.factmesh.mes.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.mes.domain.entity.WorkOrder;
import com.ly.factmesh.mes.domain.repository.WorkOrderRepository;
import com.ly.factmesh.mes.infrastructure.database.entity.WorkOrderEntity;
import com.ly.factmesh.mes.infrastructure.database.mapper.WorkOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 工单仓储实现
 *
 * @author LY-FactMesh
 */
@Repository
@RequiredArgsConstructor
public class WorkOrderRepositoryImpl implements WorkOrderRepository {

    private final WorkOrderMapper workOrderMapper;

    @Override
    public WorkOrder save(WorkOrder workOrder) {
        WorkOrderEntity entity = toEntity(workOrder);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (entity.getId() == null) {
            entity.setId(SnowflakeIdGenerator.generateId());
            if (entity.getCreateTime() == null) entity.setCreateTime(now);
            if (entity.getUpdateTime() == null) entity.setUpdateTime(now);
            workOrderMapper.insert(entity);
        } else {
            entity.setUpdateTime(now);
            workOrderMapper.updateById(entity);
        }
        return toDomain(entity);
    }

    @Override
    public Optional<WorkOrder> findById(Long id) {
        WorkOrderEntity entity = workOrderMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public Optional<WorkOrder> findByOrderCode(String orderCode) {
        LambdaQueryWrapper<WorkOrderEntity> q = new LambdaQueryWrapper<>();
        q.eq(WorkOrderEntity::getOrderCode, orderCode);
        WorkOrderEntity entity = workOrderMapper.selectOne(q);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<WorkOrder> findAll(long offset, long limit) {
        Page<WorkOrderEntity> page = new Page<>(offset / limit + 1, limit);
        Page<WorkOrderEntity> result = workOrderMapper.selectPage(page, null);
        return result.getRecords().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return workOrderMapper.selectCount(null);
    }

    @Override
    public void deleteById(Long id) {
        workOrderMapper.deleteById(id);
    }

    private WorkOrderEntity toEntity(WorkOrder domain) {
        WorkOrderEntity e = new WorkOrderEntity();
        e.setId(domain.getId());
        e.setOrderCode(domain.getOrderCode());
        e.setProductCode(domain.getProductCode());
        e.setProductName(domain.getProductName());
        e.setPlanQuantity(domain.getPlanQuantity());
        e.setActualQuantity(domain.getActualQuantity());
        e.setStatus(domain.getStatus());
        e.setStartTime(domain.getStartTime());
        e.setEndTime(domain.getEndTime());
        e.setCreateTime(domain.getCreateTime());
        e.setUpdateTime(domain.getUpdateTime());
        return e;
    }

    private WorkOrder toDomain(WorkOrderEntity e) {
        WorkOrder d = new WorkOrder();
        d.setId(e.getId());
        d.setOrderCode(e.getOrderCode());
        d.setProductCode(e.getProductCode());
        d.setProductName(e.getProductName());
        d.setPlanQuantity(e.getPlanQuantity());
        d.setActualQuantity(e.getActualQuantity());
        d.setStatus(e.getStatus());
        d.setStartTime(e.getStartTime());
        d.setEndTime(e.getEndTime());
        d.setCreateTime(e.getCreateTime());
        d.setUpdateTime(e.getUpdateTime());
        return d;
    }
}
