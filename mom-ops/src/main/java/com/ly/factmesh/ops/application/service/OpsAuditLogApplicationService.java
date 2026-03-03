package com.ly.factmesh.ops.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.ops.application.dto.OpsAuditLogDTO;
import com.ly.factmesh.ops.infrastructure.database.entity.AuditLogEntity;
import com.ly.factmesh.ops.infrastructure.database.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpsAuditLogApplicationService {

    private final AuditLogMapper auditLogMapper;

    public Page<OpsAuditLogDTO> page(int pageNum, int pageSize, String serviceName, Long userId) {
        LambdaQueryWrapper<AuditLogEntity> q = new LambdaQueryWrapper<>();
        if (serviceName != null && !serviceName.isBlank()) {
            q.eq(AuditLogEntity::getServiceName, serviceName);
        }
        if (userId != null) {
            q.eq(AuditLogEntity::getUserId, userId);
        }
        q.orderByDesc(AuditLogEntity::getOperationTime);
        Page<AuditLogEntity> p = new Page<>(pageNum, pageSize);
        Page<AuditLogEntity> result = auditLogMapper.selectPage(p, q);
        List<OpsAuditLogDTO> records = result.getRecords().stream().map(this::toDTO).collect(Collectors.toList());
        Page<OpsAuditLogDTO> page = new Page<>(pageNum, pageSize, result.getTotal());
        page.setRecords(records);
        return page;
    }

    private OpsAuditLogDTO toDTO(AuditLogEntity e) {
        OpsAuditLogDTO dto = new OpsAuditLogDTO();
        dto.setId(e.getId());
        dto.setServiceName(e.getServiceName());
        dto.setUserId(e.getUserId());
        dto.setUsername(e.getUsername());
        dto.setOperationType(e.getOperationType());
        dto.setOperationContent(e.getOperationContent());
        dto.setOperationResult(e.getOperationResult());
        dto.setOperationTime(e.getOperationTime());
        dto.setClientIp(e.getClientIp());
        dto.setRequestParams(e.getRequestParams());
        return dto;
    }
}
