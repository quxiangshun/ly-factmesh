package com.ly.factmesh.qms.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.qms.domain.entity.InspectionResult;
import com.ly.factmesh.qms.domain.repository.InspectionResultRepository;
import com.ly.factmesh.qms.infrastructure.database.entity.InspectionResultEntity;
import com.ly.factmesh.qms.infrastructure.database.mapper.InspectionResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InspectionResultRepositoryImpl implements InspectionResultRepository {

    private final InspectionResultMapper inspectionResultMapper;

    @Override
    public InspectionResult save(InspectionResult result) {
        InspectionResultEntity entity = toEntity(result);
        LocalDateTime now = LocalDateTime.now();
        if (entity.getId() == null) {
            entity.setId(SnowflakeIdGenerator.generateId());
            if (entity.getInspectionTime() == null) entity.setInspectionTime(now);
            if (entity.getCreateTime() == null) entity.setCreateTime(now);
            inspectionResultMapper.insert(entity);
        } else {
            inspectionResultMapper.updateById(entity);
        }
        return toDomain(inspectionResultMapper.selectById(entity.getId()));
    }

    @Override
    public Optional<InspectionResult> findById(Long id) {
        InspectionResultEntity entity = inspectionResultMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<InspectionResult> findByTaskId(Long taskId) {
        LambdaQueryWrapper<InspectionResultEntity> q = new LambdaQueryWrapper<>();
        q.eq(InspectionResultEntity::getTaskId, taskId).orderByAsc(InspectionResultEntity::getId);
        return inspectionResultMapper.selectList(q).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        inspectionResultMapper.deleteById(id);
    }

    private InspectionResultEntity toEntity(InspectionResult domain) {
        InspectionResultEntity e = new InspectionResultEntity();
        e.setId(domain.getId());
        e.setTaskId(domain.getTaskId());
        e.setInspectionItem(domain.getInspectionItem());
        e.setStandardValue(domain.getStandardValue());
        e.setActualValue(domain.getActualValue());
        e.setJudgment(domain.getJudgment());
        e.setInspector(domain.getInspector());
        e.setInspectionTime(domain.getInspectionTime());
        e.setCreateTime(domain.getCreateTime());
        return e;
    }

    private InspectionResult toDomain(InspectionResultEntity e) {
        InspectionResult d = new InspectionResult();
        d.setId(e.getId());
        d.setTaskId(e.getTaskId());
        d.setInspectionItem(e.getInspectionItem());
        d.setStandardValue(e.getStandardValue());
        d.setActualValue(e.getActualValue());
        d.setJudgment(e.getJudgment());
        d.setInspector(e.getInspector());
        d.setInspectionTime(e.getInspectionTime());
        d.setCreateTime(e.getCreateTime());
        return d;
    }
}
