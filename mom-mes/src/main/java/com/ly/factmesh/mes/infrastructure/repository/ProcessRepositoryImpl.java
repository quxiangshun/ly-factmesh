package com.ly.factmesh.mes.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.mes.domain.entity.Process;
import com.ly.factmesh.mes.domain.repository.ProcessRepository;
import com.ly.factmesh.mes.infrastructure.database.entity.ProcessEntity;
import com.ly.factmesh.mes.infrastructure.database.mapper.ProcessMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 工序仓储实现
 *
 * @author LY-FactMesh
 */
@Repository
@RequiredArgsConstructor
public class ProcessRepositoryImpl implements ProcessRepository {

    private final ProcessMapper processMapper;

    @Override
    public Process save(Process process) {
        ProcessEntity entity = toEntity(process);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (entity.getId() == null) {
            entity.setId(SnowflakeIdGenerator.generateId());
            if (entity.getCreateTime() == null) entity.setCreateTime(now);
            processMapper.insert(entity);
        } else {
            processMapper.updateById(entity);
        }
        return toDomain(entity);
    }

    @Override
    public Optional<Process> findById(Long id) {
        ProcessEntity entity = processMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public Optional<Process> findByProcessCode(String processCode) {
        LambdaQueryWrapper<ProcessEntity> q = new LambdaQueryWrapper<>();
        q.eq(ProcessEntity::getProcessCode, processCode);
        ProcessEntity entity = processMapper.selectOne(q);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<Process> findAll(long offset, long limit) {
        Page<ProcessEntity> page = new Page<>(offset / limit + 1, limit);
        LambdaQueryWrapper<ProcessEntity> q = new LambdaQueryWrapper<>();
        q.orderByAsc(ProcessEntity::getSequence, ProcessEntity::getId);
        Page<ProcessEntity> result = processMapper.selectPage(page, q);
        return result.getRecords().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return processMapper.selectCount(null);
    }

    @Override
    public void deleteById(Long id) {
        processMapper.deleteById(id);
    }

    private ProcessEntity toEntity(Process domain) {
        ProcessEntity e = new ProcessEntity();
        e.setId(domain.getId());
        e.setProcessCode(domain.getProcessCode());
        e.setProcessName(domain.getProcessName());
        e.setSequence(domain.getSequence());
        e.setWorkCenter(domain.getWorkCenter());
        e.setCreateTime(domain.getCreateTime());
        return e;
    }

    private Process toDomain(ProcessEntity e) {
        Process d = new Process();
        d.setId(e.getId());
        d.setProcessCode(e.getProcessCode());
        d.setProcessName(e.getProcessName());
        d.setSequence(e.getSequence());
        d.setWorkCenter(e.getWorkCenter());
        d.setCreateTime(e.getCreateTime());
        return d;
    }
}
