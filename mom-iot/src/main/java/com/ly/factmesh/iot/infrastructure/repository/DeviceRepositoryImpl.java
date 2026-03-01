package com.ly.factmesh.iot.infrastructure.repository;

import com.ly.factmesh.iot.domain.aggregate.DeviceAggregate;
import com.ly.factmesh.iot.domain.entity.Device;
import com.ly.factmesh.iot.domain.repository.DeviceRepository;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 设备仓储实现类
 * 实现DeviceRepository接口，负责设备聚合根的持久化操作
 * 使用DeviceMapper进行底层数据访问
 *
 * @author 屈想顺
 */
@Repository
@RequiredArgsConstructor
public class DeviceRepositoryImpl implements DeviceRepository {
    
    private final DeviceMapper deviceMapper;
    
    /**
     * 将设备聚合根转换为设备实体
     * @param deviceAggregate 设备聚合根
     * @return 设备实体
     */
    private Device toEntity(DeviceAggregate deviceAggregate) {
        return deviceAggregate.getDevice();
    }
    
    /**
     * 将设备实体转换为设备聚合根
     * @param device 设备实体
     * @return 设备聚合根
     */
    private DeviceAggregate toAggregate(Device device) {
        if (device == null) {
            return null;
        }
        device.rebuildStatusFromFields();
        return new DeviceAggregate(device);
    }
    
    @Override
    public DeviceAggregate save(DeviceAggregate deviceAggregate) {
        Device device = toEntity(deviceAggregate);
        if (device.getId() == null) {
            // 新增
            deviceMapper.insert(device);
        } else {
            // 更新
            deviceMapper.updateById(device);
        }
        return toAggregate(device);
    }
    
    @Override
    public Optional<DeviceAggregate> findById(Long id) {
        return Optional.ofNullable(deviceMapper.selectById(id)).map(this::toAggregate);
    }
    
    @Override
    public Optional<DeviceAggregate> findByDeviceCode(String deviceCode) {
        return Optional.ofNullable(deviceMapper.selectByDeviceCode(deviceCode)).map(this::toAggregate);
    }
    
    @Override
    public List<DeviceAggregate> findAll() {
        return deviceMapper.selectList(null).stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DeviceAggregate> findByDeviceType(String deviceType) {
        return deviceMapper.selectByDeviceType(deviceType).stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }
    
    @Override
    public void delete(DeviceAggregate deviceAggregate) {
        Device device = toEntity(deviceAggregate);
        deviceMapper.deleteById(device.getId());
    }
    
    @Override
    public void deleteById(Long id) {
        deviceMapper.deleteById(id);
    }
    
    @Override
    public boolean existsByDeviceCode(String deviceCode) {
        return deviceMapper.existsByDeviceCode(deviceCode);
    }
    
    @Override
    public long count() {
        return deviceMapper.selectCount(null);
    }

    @Override
    public long countOnline() {
        return deviceMapper.countOnline();
    }

    @Override
    public long countFault() {
        return deviceMapper.countFault();
    }
}