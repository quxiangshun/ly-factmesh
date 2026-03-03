package com.ly.factmesh.ops.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.ops.application.dto.GlobalLogCreateRequest;
import com.ly.factmesh.ops.application.dto.GlobalLogDTO;
import com.ly.factmesh.ops.infrastructure.database.entity.GlobalLogEntity;
import com.ly.factmesh.ops.infrastructure.database.mapper.GlobalLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GlobalLogApplicationService {

    private final GlobalLogMapper globalLogMapper;

    public void create(GlobalLogCreateRequest request) {
        GlobalLogEntity e = new GlobalLogEntity();
        e.setId(SnowflakeIdGenerator.generateId());
        e.setServiceName(request.getServiceName());
        e.setLogType(request.getLogType());
        e.setLogLevel(request.getLogLevel());
        e.setLogContent(request.getLogContent());
        e.setRequestId(request.getRequestId());
        e.setClientIp(request.getClientIp());
        globalLogMapper.insert(e);
    }

    public Page<GlobalLogDTO> page(int pageNum, int pageSize, String serviceName) {
        LambdaQueryWrapper<GlobalLogEntity> q = new LambdaQueryWrapper<>();
        if (serviceName != null && !serviceName.isBlank()) {
            q.eq(GlobalLogEntity::getServiceName, serviceName);
        }
        q.orderByDesc(GlobalLogEntity::getCreateTime);
        Page<GlobalLogEntity> p = new Page<>(pageNum, pageSize);
        Page<GlobalLogEntity> result = globalLogMapper.selectPage(p, q);
        List<GlobalLogDTO> records = result.getRecords().stream().map(this::toDTO).collect(Collectors.toList());
        Page<GlobalLogDTO> page = new Page<>(pageNum, pageSize, result.getTotal());
        page.setRecords(records);
        return page;
    }

    private GlobalLogDTO toDTO(GlobalLogEntity e) {
        GlobalLogDTO dto = new GlobalLogDTO();
        dto.setId(e.getId());
        dto.setServiceName(e.getServiceName());
        dto.setLogType(e.getLogType());
        dto.setLogLevel(e.getLogLevel());
        dto.setLogContent(e.getLogContent());
        dto.setRequestId(e.getRequestId());
        dto.setClientIp(e.getClientIp());
        dto.setCreateTime(e.getCreateTime());
        return dto;
    }
}
