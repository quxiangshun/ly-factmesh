package com.ly.factmesh.wms.infrastructure.repository;

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
import java.util.Optional;

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
        MaterialRequisitionDetailEntity e = new MaterialRequisitionDetailEntity();
        e.setId(detail.getId() != null ? detail.getId() : SnowflakeIdGenerator.generateId());
        e.setReqId(detail.getReqId());
        e.setMaterialId(detail.getMaterialId());
        e.setQuantity(detail.getQuantity());
        e.setActualQuantity(detail.getActualQuantity() != null ? detail.getActualQuantity() : 0);
        detailMapper.insert(e);
    }

    @Override
    public Optional<MaterialRequisition> findById(Long id) {
        MaterialRequisitionEntity e = requisitionMapper.selectById(id);
        return Optional.ofNullable(e).map(this::toDomain);
    }

    @Override
    public Optional<MaterialRequisition> findByReqNo(String reqNo) {
        return Optional.empty();
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
