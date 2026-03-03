package com.ly.factmesh.admin.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.application.dto.AuditLogCreateRequest;
import com.ly.factmesh.admin.application.dto.AuditLogDTO;
import com.ly.factmesh.admin.infrastructure.database.entity.AuditLogEntity;
import com.ly.factmesh.admin.infrastructure.database.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogApplicationService {

    private final AuditLogMapper auditLogMapper;

    public void create(AuditLogCreateRequest request) {
        AuditLogEntity e = new AuditLogEntity();
        e.setTableName(request.getTableName());
        e.setRecordId(request.getRecordId());
        e.setOperationType(request.getOperationType());
        e.setOldValue(request.getOldValue());
        e.setNewValue(request.getNewValue());
        e.setOperatorId(request.getOperatorId());
        e.setOperatorName(request.getOperatorName());
        e.setCreateTime(LocalDateTime.now());
        auditLogMapper.insert(e);
    }

    public Page<AuditLogDTO> page(int pageNum, int pageSize, String tableName, String recordId, String operationType) {
        LambdaQueryWrapper<AuditLogEntity> q = new LambdaQueryWrapper<>();
        if (tableName != null && !tableName.isBlank()) q.eq(AuditLogEntity::getTableName, tableName);
        if (recordId != null && !recordId.isBlank()) q.eq(AuditLogEntity::getRecordId, recordId);
        if (operationType != null && !operationType.isBlank()) q.eq(AuditLogEntity::getOperationType, operationType);
        q.orderByDesc(AuditLogEntity::getCreateTime);
        Page<AuditLogEntity> p = new Page<>(pageNum, pageSize);
        Page<AuditLogEntity> result = auditLogMapper.selectPage(p, q);
        List<AuditLogDTO> records = result.getRecords().stream().map(this::toDTO).collect(Collectors.toList());
        Page<AuditLogDTO> page = new Page<>(pageNum, pageSize, result.getTotal());
        page.setRecords(records);
        return page;
    }

    private AuditLogDTO toDTO(AuditLogEntity e) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(e.getId());
        dto.setTableName(e.getTableName());
        dto.setRecordId(e.getRecordId());
        dto.setOperationType(e.getOperationType());
        dto.setOldValue(e.getOldValue());
        dto.setNewValue(e.getNewValue());
        dto.setOperatorId(e.getOperatorId());
        dto.setOperatorName(e.getOperatorName());
        dto.setCreateTime(e.getCreateTime());
        return dto;
    }
}
