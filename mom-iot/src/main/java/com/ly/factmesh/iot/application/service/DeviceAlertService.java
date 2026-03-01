package com.ly.factmesh.iot.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.iot.application.dto.DeviceAlertCreateRequest;
import com.ly.factmesh.iot.application.dto.DeviceAlertDTO;
import com.ly.factmesh.iot.domain.entity.DeviceAlert;
import com.ly.factmesh.iot.domain.repository.DeviceRepository;
import com.ly.factmesh.iot.infrastructure.repository.DeviceAlertMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备告警应用服务
 *
 * @author LY-FactMesh
 */
@Service
@RequiredArgsConstructor
public class DeviceAlertService {

    private final DeviceAlertMapper deviceAlertMapper;
    private final DeviceRepository deviceRepository;

    public DeviceAlertDTO createAlert(DeviceAlertCreateRequest request) {
        deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + request.getDeviceId()));

        DeviceAlert alert = new DeviceAlert();
        alert.setId(SnowflakeIdGenerator.getInstance().nextId());
        alert.setDeviceId(request.getDeviceId());
        alert.setDeviceCode(request.getDeviceCode());
        alert.setAlertType(request.getAlertType());
        alert.setAlertLevel(request.getAlertLevel());
        alert.setAlertContent(request.getAlertContent());
        alert.setAlertStatus(0);
        alert.setCreateTime(LocalDateTime.now());
        alert.setRemark(request.getRemark());
        deviceAlertMapper.insert(alert);
        return toDTO(alert);
    }

    public DeviceAlertDTO resolveAlert(Long alertId, String resolvedBy, String remark) {
        DeviceAlert alert = deviceAlertMapper.selectById(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("告警不存在: " + alertId);
        }
        alert.setAlertStatus(1);
        alert.setResolveTime(LocalDateTime.now());
        alert.setResolvedBy(resolvedBy);
        alert.setRemark(remark != null ? remark : alert.getRemark());
        deviceAlertMapper.updateById(alert);
        return toDTO(alert);
    }

    public List<DeviceAlertDTO> listByDevice(Long deviceId, int page, int size) {
        Page<DeviceAlert> p = new Page<>(page, size);
        LambdaQueryWrapper<DeviceAlert> q = new LambdaQueryWrapper<DeviceAlert>()
                .eq(DeviceAlert::getDeviceId, deviceId)
                .orderByDesc(DeviceAlert::getCreateTime);
        return deviceAlertMapper.selectPage(p, q).getRecords().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DeviceAlertDTO> listPending(int page, int size) {
        Page<DeviceAlert> p = new Page<>(page, size);
        LambdaQueryWrapper<DeviceAlert> q = new LambdaQueryWrapper<DeviceAlert>()
                .eq(DeviceAlert::getAlertStatus, 0)
                .orderByDesc(DeviceAlert::getCreateTime);
        return deviceAlertMapper.selectPage(p, q).getRecords().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public long countPending() {
        return deviceAlertMapper.selectCount(new LambdaQueryWrapper<DeviceAlert>().eq(DeviceAlert::getAlertStatus, 0));
    }

    private DeviceAlertDTO toDTO(DeviceAlert alert) {
        return DeviceAlertDTO.builder()
                .id(alert.getId())
                .deviceId(alert.getDeviceId())
                .deviceCode(alert.getDeviceCode())
                .alertType(alert.getAlertType())
                .alertLevel(alert.getAlertLevel())
                .alertContent(alert.getAlertContent())
                .alertStatus(alert.getAlertStatus())
                .createTime(alert.getCreateTime())
                .resolveTime(alert.getResolveTime())
                .resolvedBy(alert.getResolvedBy())
                .remark(alert.getRemark())
                .build();
    }
}
