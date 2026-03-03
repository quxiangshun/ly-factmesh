package com.ly.factmesh.admin.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.application.dto.OperationLogCreateRequest;
import com.ly.factmesh.admin.application.dto.OperationLogDTO;
import com.ly.factmesh.admin.infrastructure.database.entity.OperationLogEntity;
import com.ly.factmesh.admin.infrastructure.database.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperationLogApplicationService {

    private final OperationLogMapper operationLogMapper;

    public void create(OperationLogCreateRequest request) {
        OperationLogEntity e = new OperationLogEntity();
        e.setUserId(request.getUserId());
        e.setUsername(request.getUsername());
        e.setModule(request.getModule());
        e.setOperation(request.getOperation());
        e.setMethod(request.getMethod());
        e.setUrl(request.getUrl());
        e.setParams(request.getParams());
        e.setIp(request.getIp());
        e.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        e.setErrorMsg(request.getErrorMsg());
        e.setDuration(request.getDuration());
        e.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(e);
    }

    public Page<OperationLogDTO> page(int pageNum, int pageSize, Long userId, String module, String username) {
        LambdaQueryWrapper<OperationLogEntity> q = new LambdaQueryWrapper<>();
        if (userId != null) q.eq(OperationLogEntity::getUserId, userId);
        if (module != null && !module.isBlank()) q.eq(OperationLogEntity::getModule, module);
        if (username != null && !username.isBlank()) q.like(OperationLogEntity::getUsername, username);
        q.orderByDesc(OperationLogEntity::getCreateTime);
        Page<OperationLogEntity> p = new Page<>(pageNum, pageSize);
        Page<OperationLogEntity> result = operationLogMapper.selectPage(p, q);
        List<OperationLogDTO> records = result.getRecords().stream().map(this::toDTO).collect(Collectors.toList());
        Page<OperationLogDTO> page = new Page<>(pageNum, pageSize, result.getTotal());
        page.setRecords(records);
        return page;
    }

    private OperationLogDTO toDTO(OperationLogEntity e) {
        OperationLogDTO dto = new OperationLogDTO();
        dto.setId(e.getId());
        dto.setUserId(e.getUserId());
        dto.setUsername(e.getUsername());
        dto.setModule(e.getModule());
        dto.setOperation(e.getOperation());
        dto.setMethod(e.getMethod());
        dto.setUrl(e.getUrl());
        dto.setParams(e.getParams());
        dto.setIp(e.getIp());
        dto.setStatus(e.getStatus());
        dto.setErrorMsg(e.getErrorMsg());
        dto.setDuration(e.getDuration());
        dto.setCreateTime(e.getCreateTime());
        return dto;
    }
}
