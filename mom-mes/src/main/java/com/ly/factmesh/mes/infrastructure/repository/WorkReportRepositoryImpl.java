package com.ly.factmesh.mes.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.mes.domain.entity.WorkReport;
import com.ly.factmesh.mes.domain.repository.WorkReportRepository;
import com.ly.factmesh.mes.infrastructure.database.entity.WorkReportEntity;
import com.ly.factmesh.mes.infrastructure.database.mapper.WorkReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 报工记录仓储实现
 *
 * @author LY-FactMesh
 */
@Repository
@RequiredArgsConstructor
public class WorkReportRepositoryImpl implements WorkReportRepository {

    private final WorkReportMapper workReportMapper;

    @Override
    public WorkReport save(WorkReport workReport) {
        WorkReportEntity entity = toEntity(workReport);
        if (entity.getId() == null) {
            entity.setId(SnowflakeIdGenerator.generateId());
            if (entity.getReportTime() == null) {
                entity.setReportTime(java.time.LocalDateTime.now());
            }
            workReportMapper.insert(entity);
        } else {
            workReportMapper.updateById(entity);
        }
        return toDomain(entity);
    }

    @Override
    public Optional<WorkReport> findById(Long id) {
        WorkReportEntity entity = workReportMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<WorkReport> findByOrderId(Long orderId, long offset, long limit) {
        Page<WorkReportEntity> page = new Page<>(offset / limit + 1, limit);
        LambdaQueryWrapper<WorkReportEntity> q = new LambdaQueryWrapper<>();
        q.eq(WorkReportEntity::getOrderId, orderId);
        q.orderByDesc(WorkReportEntity::getReportTime);
        Page<WorkReportEntity> result = workReportMapper.selectPage(page, q);
        return result.getRecords().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<WorkReport> findAll(long offset, long limit) {
        Page<WorkReportEntity> page = new Page<>(offset / limit + 1, limit);
        LambdaQueryWrapper<WorkReportEntity> q = new LambdaQueryWrapper<>();
        q.orderByDesc(WorkReportEntity::getReportTime);
        Page<WorkReportEntity> result = workReportMapper.selectPage(page, q);
        return result.getRecords().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return workReportMapper.selectCount(null);
    }

    @Override
    public long countByOrderId(Long orderId) {
        LambdaQueryWrapper<WorkReportEntity> q = new LambdaQueryWrapper<>();
        q.eq(WorkReportEntity::getOrderId, orderId);
        return workReportMapper.selectCount(q);
    }

    @Override
    public void deleteById(Long id) {
        workReportMapper.deleteById(id);
    }

    private WorkReportEntity toEntity(WorkReport domain) {
        WorkReportEntity e = new WorkReportEntity();
        e.setId(domain.getId());
        e.setOrderId(domain.getOrderId());
        e.setProcessId(domain.getProcessId());
        e.setDeviceId(domain.getDeviceId());
        e.setReportQuantity(domain.getReportQuantity());
        e.setScrapQuantity(domain.getScrapQuantity() != null ? domain.getScrapQuantity() : 0);
        e.setReportTime(domain.getReportTime());
        e.setOperator(domain.getOperator());
        return e;
    }

    private WorkReport toDomain(WorkReportEntity e) {
        WorkReport d = new WorkReport();
        d.setId(e.getId());
        d.setOrderId(e.getOrderId());
        d.setProcessId(e.getProcessId());
        d.setDeviceId(e.getDeviceId());
        d.setReportQuantity(e.getReportQuantity());
        d.setScrapQuantity(e.getScrapQuantity());
        d.setReportTime(e.getReportTime());
        d.setOperator(e.getOperator());
        return d;
    }
}
