package com.ly.factmesh.qms.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.qms.domain.entity.NonConformingProduct;
import com.ly.factmesh.qms.domain.repository.NonConformingProductRepository;
import com.ly.factmesh.qms.infrastructure.database.entity.NonConformingProductEntity;
import com.ly.factmesh.qms.infrastructure.database.mapper.NonConformingProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class NonConformingProductRepositoryImpl implements NonConformingProductRepository {

    private final NonConformingProductMapper nonConformingProductMapper;

    @Override
    public NonConformingProduct save(NonConformingProduct product) {
        NonConformingProductEntity entity = toEntity(product);
        LocalDateTime now = LocalDateTime.now();
        if (entity.getId() == null) {
            entity.setId(SnowflakeIdGenerator.generateId());
            if (entity.getCreateTime() == null) entity.setCreateTime(now);
            nonConformingProductMapper.insert(entity);
        } else {
            nonConformingProductMapper.updateById(entity);
        }
        return toDomain(nonConformingProductMapper.selectById(entity.getId()));
    }

    @Override
    public Optional<NonConformingProduct> findById(Long id) {
        NonConformingProductEntity entity = nonConformingProductMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<NonConformingProduct> findAll(long offset, long limit, Integer disposalResult, Long taskId) {
        long pageNum = limit <= 0 ? 1 : offset / limit + 1;
        Page<NonConformingProductEntity> page = new Page<>(pageNum, limit);
        LambdaQueryWrapper<NonConformingProductEntity> q = new LambdaQueryWrapper<>();
        if (disposalResult != null) {
            q.eq(NonConformingProductEntity::getDisposalResult, disposalResult);
        }
        if (taskId != null) {
            q.eq(NonConformingProductEntity::getTaskId, taskId);
        }
        q.orderByDesc(NonConformingProductEntity::getCreateTime);
        Page<NonConformingProductEntity> result = nonConformingProductMapper.selectPage(page, q);
        return result.getRecords().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<NonConformingProduct> findByTaskId(Long taskId) {
        LambdaQueryWrapper<NonConformingProductEntity> q = new LambdaQueryWrapper<>();
        q.eq(NonConformingProductEntity::getTaskId, taskId).orderByDesc(NonConformingProductEntity::getCreateTime);
        return nonConformingProductMapper.selectList(q).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countByDisposalResult(Integer disposalResult) {
        return countByDisposalResultAndTaskId(disposalResult, null);
    }

    @Override
    public long countByDisposalResultAndTaskId(Integer disposalResult, Long taskId) {
        LambdaQueryWrapper<NonConformingProductEntity> q = new LambdaQueryWrapper<>();
        if (disposalResult != null) {
            q.eq(NonConformingProductEntity::getDisposalResult, disposalResult);
        }
        if (taskId != null) {
            q.eq(NonConformingProductEntity::getTaskId, taskId);
        }
        return nonConformingProductMapper.selectCount(q);
    }

    @Override
    public Optional<NonConformingProduct> findByNcrNo(String ncrNo) {
        if (ncrNo == null || ncrNo.isBlank()) return Optional.empty();
        LambdaQueryWrapper<NonConformingProductEntity> q = new LambdaQueryWrapper<>();
        q.eq(NonConformingProductEntity::getNcrNo, ncrNo);
        NonConformingProductEntity entity = nonConformingProductMapper.selectOne(q);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        nonConformingProductMapper.deleteById(id);
    }

    private NonConformingProductEntity toEntity(NonConformingProduct domain) {
        NonConformingProductEntity e = new NonConformingProductEntity();
        e.setId(domain.getId());
        e.setNcrNo(domain.getNcrNo());
        e.setProductCode(domain.getProductCode());
        e.setBatchNo(domain.getBatchNo());
        e.setQuantity(domain.getQuantity());
        e.setReason(domain.getReason());
        e.setDisposalMethod(domain.getDisposalMethod());
        e.setDisposalResult(domain.getDisposalResult());
        e.setTaskId(domain.getTaskId());
        e.setCreateTime(domain.getCreateTime());
        e.setDisposeTime(domain.getDisposeTime());
        e.setRemark(domain.getRemark());
        return e;
    }

    private NonConformingProduct toDomain(NonConformingProductEntity e) {
        NonConformingProduct d = new NonConformingProduct();
        d.setId(e.getId());
        d.setNcrNo(e.getNcrNo());
        d.setProductCode(e.getProductCode());
        d.setBatchNo(e.getBatchNo());
        d.setQuantity(e.getQuantity());
        d.setReason(e.getReason());
        d.setDisposalMethod(e.getDisposalMethod());
        d.setDisposalResult(e.getDisposalResult());
        d.setTaskId(e.getTaskId());
        d.setCreateTime(e.getCreateTime());
        d.setDisposeTime(e.getDisposeTime());
        d.setRemark(e.getRemark());
        return d;
    }
}
