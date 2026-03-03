package com.ly.factmesh.ops.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.ops.application.dto.SystemEventDTO;
import com.ly.factmesh.ops.infrastructure.database.entity.SystemEventEntity;
import com.ly.factmesh.ops.infrastructure.database.mapper.SystemEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemEventApplicationService {

    private final SystemEventMapper systemEventMapper;

    public Page<SystemEventDTO> page(int pageNum, int pageSize, String eventType, Integer processed) {
        LambdaQueryWrapper<SystemEventEntity> q = new LambdaQueryWrapper<>();
        if (eventType != null && !eventType.isBlank()) {
            q.eq(SystemEventEntity::getEventType, eventType);
        }
        if (processed != null) {
            q.eq(SystemEventEntity::getProcessed, processed);
        }
        q.orderByDesc(SystemEventEntity::getCreateTime);
        Page<SystemEventEntity> p = new Page<>(pageNum, pageSize);
        Page<SystemEventEntity> result = systemEventMapper.selectPage(p, q);
        List<SystemEventDTO> records = result.getRecords().stream().map(this::toDTO).collect(Collectors.toList());
        Page<SystemEventDTO> page = new Page<>(pageNum, pageSize, result.getTotal());
        page.setRecords(records);
        return page;
    }

    private SystemEventDTO toDTO(SystemEventEntity e) {
        SystemEventDTO dto = new SystemEventDTO();
        dto.setId(e.getId());
        dto.setEventType(e.getEventType());
        dto.setEventLevel(e.getEventLevel());
        dto.setEventContent(e.getEventContent());
        dto.setRelatedService(e.getRelatedService());
        dto.setRelatedId(e.getRelatedId());
        dto.setCreateTime(e.getCreateTime());
        dto.setProcessed(e.getProcessed());
        dto.setProcessTime(e.getProcessTime());
        return dto;
    }
}
