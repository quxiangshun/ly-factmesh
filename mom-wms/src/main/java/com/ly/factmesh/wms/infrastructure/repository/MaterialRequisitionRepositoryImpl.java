package com.ly.factmesh.wms.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.wms.domain.entity.MaterialRequisition;
import com.ly.factmesh.wms.domain.entity.MaterialRequisitionDetail;
import com.ly.factmesh.wms.domain.repository.MaterialRequisitionRepository;
import com.ly.factmesh.wms.infrastructure.database.entity.MaterialRequisitionDetailEntity;
import com.ly.factmesh.wms.infrastructure.database.entity.MaterialRequisitionEntity;
import com.ly.factmesh.wms.infrastructure.database.mapper.MaterialRequisitionDetailMapper;
import com.ly.factmesh.wms.infrastructure.database.mapper.MaterialRequisitionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MaterialRequisitionRepositoryImpl implements MaterialRequisitionRepository {

    private final MaterialRequisitionMapper requisitionMapper;
    private final MaterialRequisitionDetailMapper detailMapper;

    @Override
    public MaterialRequisition save(MaterialRequisition req) {
        MaterialRequisitionEntity e = toEntity(req);
        LocalDateTime now = LocalDateTime.now();
        if (e.getId() == null) {
            e.setId(SnowflakeIdGenerator.generateId());
            if (e.getCreateTime() == null) e.setCreateTime(now);
            if (e.getUpdateTime() == null) e.setUpdateTime(now);
            requisitionMapper.insert(e);
        } else {
            e.setUpdateTime(now);
            requisitionMapper.updateById(e);
        }
        return toDomain(e);
    }

    @Override
    public void saveDetail(MaterialRequisitionDetail detail) {
        MaterialRequisitionDetailEntity e = toDetailEntity(detail);
        e.setId(detail.getId() != null ? detail.getId() : SnowflakeIdGenerator.generateId());
        e.setActualQuantity(detail.getActualQuantity() != null ? detail.getActualQuantity() : 0);
        detailMapper.insert(e);
    }

    @Override
    public void updateDetail(MaterialRequisitionDetail detail) {
        MaterialRequisitionDetailEntity e = toDetailEntity(detail);
        detailMapper.updateById(e);
    }

    @Override
    public Optional<MaterialRequisition> findById(Long id) {
        MaterialRequisitionEntity e = requisitionMapper.selectById(id);
        return Optional.ofNullable(e).map(this::toDomain);
    }

    @Override
    public Optional<MaterialRequisition> findByReqNo(String reqNo) {
        if (reqNo == null || reqNo.isBlank()) return Optional.empty();
        LambdaQueryWrapper<MaterialRequisitionEntity> q = new LambdaQueryWrapper<>();
        q.eq(MaterialRequisitionEntity::getReqNo, reqNo);
        MaterialRequisitionEntity e = requisitionMapper.selectOne(q);
        return Optional.ofNullable(e).map(this::toDomain);
    }

    @Override
    public List<MaterialRequisition> findAll(long offset, long limit, Long orderId, Integer status) {
        long pageNum = limit <= 0 ? 1 : offset / limit + 1;
        Page<MaterialRequisitionEntity> page = new Page<>(pageNum, limit);
        LambdaQueryWrapper<MaterialRequisitionEntity> q = new LambdaQueryWrapper<>();
        if (orderId != null) q.eq(MaterialRequisitionEntity::getOrderId, orderId);
        if (status != null) q.eq(MaterialRequisitionEntity::getStatus, status);
        q.orderByDesc(MaterialRequisitionEntity::getCreateTime);
        Page<MaterialRequisitionEntity> result = requisitionMapper.selectPage(page, q);
        return result.getRecords().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count(Long orderId, Integer status) {
        LambdaQueryWrapper<MaterialRequisitionEntity> q = new LambdaQueryWrapper<>();
        if (orderId != null) q.eq(MaterialRequisitionEntity::getOrderId, orderId);
        if (status != null) q.eq(MaterialRequisitionEntity::getStatus, status);
        return requisitionMapper.selectCount(q);
    }

    @Override
    public List<MaterialRequisitionDetail> findDetailsByReqId(Long reqId) {
        if (reqId == null) return List.of();
        LambdaQueryWrapper<MaterialRequisitionDetailEntity> q = new LambdaQueryWrapper<>();
        q.eq(MaterialRequisitionDetailEntity::getReqId, reqId);
        return detailMapper.selectList(q).stream().map(this::toDetailDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsDetailByMaterialId(Long materialId) {
        if (materialId == null) return false;
        LambdaQueryWrapper<MaterialRequisitionDetailEntity> q = new LambdaQueryWrapper<>();
        q.eq(MaterialRequisitionDetailEntity::getMaterialId, materialId);
        return detailMapper.selectCount(q) > 0;
    }

    private MaterialRequisitionDetailEntity toDetailEntity(MaterialRequisitionDetail d) {
        MaterialRequisitionDetailEntity e = new MaterialRequisitionDetailEntity();
        e.setId(d.getId());
        e.setReqId(d.getReqId());
        e.setMaterialId(d.getMaterialId());
        e.setBatchNo(d.getBatchNo());
        e.setQuantity(d.getQuantity());
        e.setActualQuantity(d.getActualQuantity() != null ? d.getActualQuantity() : 0);
        return e;
    }

    private MaterialRequisitionDetail toDetailDomain(MaterialRequisitionDetailEntity e) {
        MaterialRequisitionDetail d = new MaterialRequisitionDetail();
        d.setId(e.getId());
        d.setReqId(e.getReqId());
        d.setMaterialId(e.getMaterialId());
        d.setBatchNo(e.getBatchNo());
        d.setQuantity(e.getQuantity());
        d.setActualQuantity(e.getActualQuantity());
        return d;
    }

    private MaterialRequisitionEntity toEntity(MaterialRequisition d) {
        MaterialRequisitionEntity e = new MaterialRequisitionEntity();
        e.setId(d.getId());
        e.setReqNo(d.getReqNo());
        e.setOrderId(d.getOrderId());
        e.setReqType(d.getReqType());
        e.setStatus(d.getStatus());
        e.setCreateTime(d.getCreateTime());
        e.setUpdateTime(d.getUpdateTime());
        return e;
    }

    private MaterialRequisition toDomain(MaterialRequisitionEntity e) {
        MaterialRequisition d = new MaterialRequisition();
        d.setId(e.getId());
        d.setReqNo(e.getReqNo());
        d.setOrderId(e.getOrderId());
        d.setReqType(e.getReqType());
        d.setStatus(e.getStatus());
        d.setCreateTime(e.getCreateTime());
        d.setUpdateTime(e.getUpdateTime());
        return d;
    }
}
