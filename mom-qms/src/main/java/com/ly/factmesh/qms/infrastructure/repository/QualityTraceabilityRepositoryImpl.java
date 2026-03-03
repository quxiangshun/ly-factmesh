package com.ly.factmesh.qms.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.qms.domain.entity.QualityTraceability;
import com.ly.factmesh.qms.domain.repository.QualityTraceabilityRepository;
import com.ly.factmesh.qms.infrastructure.database.entity.QualityTraceabilityEntity;
import com.ly.factmesh.qms.infrastructure.database.mapper.QualityTraceabilityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 质量追溯仓储实现
 *
 * @author LY-FactMesh
 */
@Repository
@RequiredArgsConstructor
public class QualityTraceabilityRepositoryImpl implements QualityTraceabilityRepository {

    private final QualityTraceabilityMapper qualityTraceabilityMapper;

    @Override
    public QualityTraceability save(QualityTraceability trace) {
        QualityTraceabilityEntity e = toEntity(trace);
        if (e.getId() == null) {
            e.setId(SnowflakeIdGenerator.generateId());
            if (e.getCreateTime() == null) e.setCreateTime(LocalDateTime.now());
            qualityTraceabilityMapper.insert(e);
        } else {
            qualityTraceabilityMapper.updateById(e);
        }
        return toDomain(qualityTraceabilityMapper.selectById(e.getId()));
    }

    @Override
    public List<QualityTraceability> findByProductCode(String productCode) {
        return query(q -> q.eq(QualityTraceabilityEntity::getProductCode, productCode));
    }

    @Override
    public List<QualityTraceability> findByBatchNo(String batchNo) {
        return query(q -> q.eq(QualityTraceabilityEntity::getBatchNo, batchNo));
    }

    @Override
    public List<QualityTraceability> findByProductionOrder(String productionOrder) {
        return query(q -> q.eq(QualityTraceabilityEntity::getProductionOrder, productionOrder));
    }

    @Override
    public List<QualityTraceability> findByProductCodeAndBatchNo(String productCode, String batchNo) {
        return query(q -> {
            q.eq(QualityTraceabilityEntity::getProductCode, productCode);
            if (batchNo != null && !batchNo.isBlank()) {
                q.eq(QualityTraceabilityEntity::getBatchNo, batchNo);
            }
        });
    }

    public List<QualityTraceability> findTrace(String productCode, String batchNo, String productionOrder) {
        LambdaQueryWrapper<QualityTraceabilityEntity> q = new LambdaQueryWrapper<>();
        if (productCode != null && !productCode.isBlank()) {
            q.eq(QualityTraceabilityEntity::getProductCode, productCode);
        }
        if (batchNo != null && !batchNo.isBlank()) {
            q.eq(QualityTraceabilityEntity::getBatchNo, batchNo);
        }
        if (productionOrder != null && !productionOrder.isBlank()) {
            q.eq(QualityTraceabilityEntity::getProductionOrder, productionOrder);
        }
        q.orderByDesc(QualityTraceabilityEntity::getCreateTime);
        return qualityTraceabilityMapper.selectList(q).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private List<QualityTraceability> query(java.util.function.Consumer<LambdaQueryWrapper<QualityTraceabilityEntity>> fn) {
        LambdaQueryWrapper<QualityTraceabilityEntity> q = new LambdaQueryWrapper<>();
        fn.accept(q);
        q.orderByDesc(QualityTraceabilityEntity::getCreateTime);
        return qualityTraceabilityMapper.selectList(q).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private QualityTraceabilityEntity toEntity(QualityTraceability d) {
        QualityTraceabilityEntity e = new QualityTraceabilityEntity();
        e.setId(d.getId());
        e.setProductCode(d.getProductCode());
        e.setBatchNo(d.getBatchNo());
        e.setMaterialBatch(d.getMaterialBatch());
        e.setProductionOrder(d.getProductionOrder());
        e.setInspectionRecordId(d.getInspectionRecordId());
        e.setNonConformingId(d.getNonConformingId());
        e.setCreateTime(d.getCreateTime());
        return e;
    }

    private QualityTraceability toDomain(QualityTraceabilityEntity e) {
        if (e == null) return null;
        QualityTraceability d = new QualityTraceability();
        d.setId(e.getId());
        d.setProductCode(e.getProductCode());
        d.setBatchNo(e.getBatchNo());
        d.setMaterialBatch(e.getMaterialBatch());
        d.setProductionOrder(e.getProductionOrder());
        d.setInspectionRecordId(e.getInspectionRecordId());
        d.setNonConformingId(e.getNonConformingId());
        d.setCreateTime(e.getCreateTime());
        return d;
    }
}
