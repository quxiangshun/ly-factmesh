package com.ly.factmesh.iot.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.iot.application.dto.DeviceMaintenanceCreateRequest;
import com.ly.factmesh.iot.application.dto.DeviceMaintenanceDTO;
import com.ly.factmesh.iot.application.dto.DeviceMaintenanceUpdateRequest;
import com.ly.factmesh.iot.domain.entity.Device;
import com.ly.factmesh.iot.domain.entity.DeviceMaintenance;
import com.ly.factmesh.iot.infrastructure.repository.DeviceMaintenanceMapper;
import com.ly.factmesh.iot.infrastructure.repository.DeviceMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备维保记录应用服务
 *
 * @author LY-FactMesh
 */
@Service
public class DeviceMaintenanceService {

    private final DeviceMaintenanceMapper deviceMaintenanceMapper;
    private final DeviceMapper deviceMapper;

    public DeviceMaintenanceService(DeviceMaintenanceMapper deviceMaintenanceMapper,
                                    DeviceMapper deviceMapper) {
        this.deviceMaintenanceMapper = deviceMaintenanceMapper;
        this.deviceMapper = deviceMapper;
    }

    public DeviceMaintenanceDTO create(DeviceMaintenanceCreateRequest request) {
        Device device = deviceMapper.selectById(request.getDeviceId());
        if (device == null) {
            throw new IllegalArgumentException("设备不存在: " + request.getDeviceId());
        }
        DeviceMaintenance m = new DeviceMaintenance();
        m.setId(SnowflakeIdGenerator.getInstance().nextId());
        m.setDeviceId(request.getDeviceId());
        m.setMaintenanceType(request.getMaintenanceType());
        m.setMaintenanceDate(request.getMaintenanceDate());
        m.setContent(request.getContent());
        m.setOperatorName(request.getOperatorName());
        m.setCost(request.getCost());
        m.setRemark(request.getRemark());
        m.setCreateTime(LocalDateTime.now());
        m.setUpdateTime(LocalDateTime.now());
        deviceMaintenanceMapper.insert(m);
        return toDTO(m, device);
    }

    public DeviceMaintenanceDTO update(Long id, DeviceMaintenanceUpdateRequest request) {
        DeviceMaintenance m = deviceMaintenanceMapper.selectById(id);
        if (m == null) {
            throw new IllegalArgumentException("维保记录不存在: " + id);
        }
        if (request.getMaintenanceType() != null) m.setMaintenanceType(request.getMaintenanceType());
        if (request.getMaintenanceDate() != null) m.setMaintenanceDate(request.getMaintenanceDate());
        if (request.getContent() != null) m.setContent(request.getContent());
        if (request.getOperatorName() != null) m.setOperatorName(request.getOperatorName());
        if (request.getCost() != null) m.setCost(request.getCost());
        if (request.getRemark() != null) m.setRemark(request.getRemark());
        m.setUpdateTime(LocalDateTime.now());
        deviceMaintenanceMapper.updateById(m);
        Device device = deviceMapper.selectById(m.getDeviceId());
        return toDTO(m, device);
    }

    public void delete(Long id) {
        deviceMaintenanceMapper.deleteById(id);
    }

    public DeviceMaintenanceDTO getById(Long id) {
        DeviceMaintenance m = deviceMaintenanceMapper.selectById(id);
        if (m == null) return null;
        Device device = deviceMapper.selectById(m.getDeviceId());
        return toDTO(m, device);
    }

    public List<DeviceMaintenanceDTO> listByDevice(Long deviceId, int page, int size) {
        Page<DeviceMaintenance> p = new Page<>(page, size);
        LambdaQueryWrapper<DeviceMaintenance> q = new LambdaQueryWrapper<DeviceMaintenance>()
                .eq(DeviceMaintenance::getDeviceId, deviceId)
                .orderByDesc(DeviceMaintenance::getMaintenanceDate);
        List<DeviceMaintenance> records = deviceMaintenanceMapper.selectPage(p, q).getRecords();
        Device device = deviceMapper.selectById(deviceId);
        return records.stream().map(r -> toDTO(r, device)).collect(Collectors.toList());
    }

    public List<DeviceMaintenanceDTO> listAll(int page, int size) {
        Page<DeviceMaintenance> p = new Page<>(page, size);
        LambdaQueryWrapper<DeviceMaintenance> q = new LambdaQueryWrapper<DeviceMaintenance>()
                .orderByDesc(DeviceMaintenance::getMaintenanceDate);
        List<DeviceMaintenance> records = deviceMaintenanceMapper.selectPage(p, q).getRecords();
        return records.stream().map(r -> {
            Device device = deviceMapper.selectById(r.getDeviceId());
            return toDTO(r, device);
        }).collect(Collectors.toList());
    }

    private DeviceMaintenanceDTO toDTO(DeviceMaintenance m, Device device) {
        return DeviceMaintenanceDTO.builder()
                .id(m.getId())
                .deviceId(m.getDeviceId())
                .deviceCode(device != null ? device.getDeviceCode() : null)
                .deviceName(device != null ? device.getDeviceName() : null)
                .maintenanceType(m.getMaintenanceType())
                .maintenanceDate(m.getMaintenanceDate())
                .content(m.getContent())
                .operatorName(m.getOperatorName())
                .cost(m.getCost())
                .remark(m.getRemark())
                .createTime(m.getCreateTime())
                .updateTime(m.getUpdateTime())
                .build();
    }
}
