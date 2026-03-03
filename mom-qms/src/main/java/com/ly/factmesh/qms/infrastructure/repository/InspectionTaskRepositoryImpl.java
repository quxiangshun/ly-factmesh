package com.ly.factmesh.qms.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.qms.domain.entity.InspectionTask;
import com.ly.factmesh.qms.domain.repository.InspectionTaskRepository;
import com.ly.factmesh.qms.infrastructure.database.entity.InspectionTaskEntity;
import com.ly.factmesh.qms.infrastructure.database.mapper.InspectionTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InspectionTaskRepositoryImpl implements InspectionTaskRepository {

    private final InspectionTaskMapper inspectionTaskMapper;

    @Override
    public InspectionTask save(InspectionTask task) {
        InspectionTaskEntity entity = toEntity(task);
        LocalDateTime now = LocalDateTime.now();
        if (entity.getId() == null) {
            entity.setId(SnowflakeIdGenerator.generateId());
            if (entity.getCreateTime() == null) entity.setCreateTime(now);
            if (entity.getUpdateTime() == null) entity.setUpdateTime(now);
            inspectionTaskMapper.insert(entity);
        } else {
            entity.setUpdateTime(now);
            inspectionTaskMapper.updateById(entity);
        }
        return toDomain(entity);
    }

    @Override
    public Optional<InspectionTask> findById(Long id) {
        InspectionTaskEntity entity = inspectionTaskMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public Optional<InspectionTask> findByTaskCode(String taskCode) {
        LambdaQueryWrapper<InspectionTaskEntity> q = new LambdaQueryWrapper<>();
        q.eq(InspectionTaskEntity::getTaskCode, taskCode);
        InspectionTaskEntity entity = inspectionTaskMapper.selectOne(q);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<InspectionTask> findAll(long offset, long limit) {
        return findAll(offset, limit, null);
    }

    @Override
    public List<InspectionTask> findAll(long offset, long limit, Integer status) {
        long pageNum = limit <= 0 ? 1 : offset / limit + 1;
        Page<InspectionTaskEntity> page = new Page<>(pageNum, limit);
        LambdaQueryWrapper<InspectionTaskEntity> q = new LambdaQueryWrapper<>();
        if (status != null) {
            q.eq(InspectionTaskEntity::getStatus, status);
        }
        q.orderByDesc(InspectionTaskEntity::getCreateTime);
        Page<InspectionTaskEntity> result = inspectionTaskMapper.selectPage(page, q);
        return result.getRecords().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return countByStatus(null);
    }

    @Override
    public long countByStatus(Integer status) {
        LambdaQueryWrapper<InspectionTaskEntity> q = new LambdaQueryWrapper<>();
        if (status != null) {
            q.eq(InspectionTaskEntity::getStatus, status);
        }
        return inspectionTaskMapper.selectCount(q);
    }

    @Override
    public void deleteById(Long id) {
        inspectionTaskMapper.deleteById(id);
    }

    private InspectionTaskEntity toEntity(InspectionTask domain) {
        InspectionTaskEntity e = new InspectionTaskEntity();
        e.setId(domain.getId());
        e.setTaskCode(domain.getTaskCode());
        e.setOrderId(domain.getOrderId());
        e.setOrderCode(domain.getOrderCode());
        e.setProductCode(domain.getProductCode());
        e.setMaterialId(domain.getMaterialId());
        e.setDeviceId(domain.getDeviceId());
        e.setInspectionType(domain.getInspectionType());
        e.setInspectionTime(domain.getInspectionTime());
        e.setStatus(domain.getStatus());
        e.setOperator(domain.getOperator());
        e.setCreateTime(domain.getCreateTime());
        e.setUpdateTime(domain.getUpdateTime());
        return e;
    }

    private InspectionTask toDomain(InspectionTaskEntity e) {
        InspectionTask d = new InspectionTask();
        d.setId(e.getId());
        d.setTaskCode(e.getTaskCode());
        d.setOrderId(e.getOrderId());
        d.setOrderCode(e.getOrderCode());
        d.setProductCode(e.getProductCode());
        d.setMaterialId(e.getMaterialId());
        d.setDeviceId(e.getDeviceId());
        d.setInspectionType(e.getInspectionType());
        d.setInspectionTime(e.getInspectionTime());
        d.setStatus(e.getStatus());
        d.setOperator(e.getOperator());
        d.setCreateTime(e.getCreateTime());
        d.setUpdateTime(e.getUpdateTime());
        return d;
    }
}
