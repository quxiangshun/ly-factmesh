package com.ly.factmesh.iot.domain.service;

import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.iot.domain.aggregate.DeviceAggregate;
import com.ly.factmesh.iot.domain.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

/**
 * 设备领域服务
 * 封装设备相关的核心业务逻辑
 * 处理跨实体或聚合根的业务规则
 *
 * @author 屈想顺
 */
@Service
@RequiredArgsConstructor
public class DeviceDomainService {
    
    private final DeviceRepository deviceRepository;
    
    /**
     * 注册新设备
     * @param deviceCode 设备编码
     * @param deviceName 设备名称
     * @param deviceType 设备类型
     * @param model 设备型号
     * @param manufacturer 制造商
     * @param installLocation 安装位置
     * @return 设备聚合根
     */
    public DeviceAggregate registerDevice(String deviceCode, String deviceName, String deviceType, String model, String manufacturer, String installLocation) {
        // 1. 检查设备编码是否已存在
        if (deviceRepository.existsByDeviceCode(deviceCode)) {
            throw new IllegalArgumentException("设备编码已存在：" + deviceCode);
        }
        
        // 2. 生成设备ID
        Long deviceId = SnowflakeIdGenerator.getInstance().nextId();
        
        // 3. 创建设备聚合根
        DeviceAggregate deviceAggregate = new DeviceAggregate();
        deviceAggregate.getDevice().setId(deviceId);
        deviceAggregate.getDevice().setDeviceCode(deviceCode);
        deviceAggregate.getDevice().setDeviceName(deviceName);
        deviceAggregate.getDevice().setDeviceType(deviceType);
        deviceAggregate.getDevice().setModel(model);
        deviceAggregate.getDevice().setManufacturer(manufacturer);
        deviceAggregate.getDevice().setInstallLocation(installLocation);
        
        // 4. 调用聚合根的注册方法
        deviceAggregate.registerDevice();
        
        // 5. 保存设备聚合根
        return deviceRepository.save(deviceAggregate);
    }
    
    /**
     * 更新设备信息
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     * @param deviceType 设备类型
     * @param model 设备型号
     * @param manufacturer 制造商
     * @param installLocation 安装位置
     * @return 更新后的设备聚合根
     */
    public DeviceAggregate updateDeviceInfo(Long deviceId, String deviceName, String deviceType, String model, String manufacturer, String installLocation) {
        // 1. 根据ID获取设备聚合根
        Optional<DeviceAggregate> optionalDeviceAggregate = deviceRepository.findById(deviceId);
        if (optionalDeviceAggregate.isEmpty()) {
            throw new IllegalArgumentException("设备不存在：ID=" + deviceId);
        }
        
        // 2. 更新设备信息
        DeviceAggregate deviceAggregate = optionalDeviceAggregate.get();
        deviceAggregate.updateDeviceInfo(deviceName, deviceType, model, manufacturer, installLocation);
        
        // 3. 保存更新后的设备聚合根
        return deviceRepository.save(deviceAggregate);
    }
    
    /**
     * 设备上线
     * @param deviceId 设备ID
     * @return 更新后的设备聚合根
     */
    public DeviceAggregate deviceOnline(Long deviceId) {
        // 1. 根据ID获取设备聚合根
        Optional<DeviceAggregate> optionalDeviceAggregate = deviceRepository.findById(deviceId);
        if (optionalDeviceAggregate.isEmpty()) {
            throw new IllegalArgumentException("设备不存在：ID=" + deviceId);
        }
        
        // 2. 调用聚合根的上线方法
        DeviceAggregate deviceAggregate = optionalDeviceAggregate.get();
        deviceAggregate.deviceOnline();
        
        // 3. 保存更新后的设备聚合根
        return deviceRepository.save(deviceAggregate);
    }
    
    /**
     * 设备离线
     * @param deviceId 设备ID
     * @return 更新后的设备聚合根
     */
    public DeviceAggregate deviceOffline(Long deviceId) {
        // 1. 根据ID获取设备聚合根
        Optional<DeviceAggregate> optionalDeviceAggregate = deviceRepository.findById(deviceId);
        if (optionalDeviceAggregate.isEmpty()) {
            throw new IllegalArgumentException("设备不存在：ID=" + deviceId);
        }
        
        // 2. 调用聚合根的离线方法
        DeviceAggregate deviceAggregate = optionalDeviceAggregate.get();
        deviceAggregate.deviceOffline();
        
        // 3. 保存更新后的设备聚合根
        return deviceRepository.save(deviceAggregate);
    }
    
    /**
     * 设备开始运行
     * @param deviceId 设备ID
     * @return 更新后的设备聚合根
     */
    public DeviceAggregate startRunning(Long deviceId) {
        // 1. 根据ID获取设备聚合根
        Optional<DeviceAggregate> optionalDeviceAggregate = deviceRepository.findById(deviceId);
        if (optionalDeviceAggregate.isEmpty()) {
            throw new IllegalArgumentException("设备不存在：ID=" + deviceId);
        }
        
        // 2. 调用聚合根的开始运行方法
        DeviceAggregate deviceAggregate = optionalDeviceAggregate.get();
        deviceAggregate.startRunning();
        
        // 3. 保存更新后的设备聚合根
        return deviceRepository.save(deviceAggregate);
    }
    
    /**
     * 设备停止运行
     * @param deviceId 设备ID
     * @return 更新后的设备聚合根
     */
    public DeviceAggregate stopRunning(Long deviceId) {
        // 1. 根据ID获取设备聚合根
        Optional<DeviceAggregate> optionalDeviceAggregate = deviceRepository.findById(deviceId);
        if (optionalDeviceAggregate.isEmpty()) {
            throw new IllegalArgumentException("设备不存在：ID=" + deviceId);
        }
        
        // 2. 调用聚合根的停止运行方法
        DeviceAggregate deviceAggregate = optionalDeviceAggregate.get();
        deviceAggregate.stopRunning();
        
        // 3. 保存更新后的设备聚合根
        return deviceRepository.save(deviceAggregate);
    }
    
    /**
     * 设备故障
     * @param deviceId 设备ID
     * @return 更新后的设备聚合根
     */
    public DeviceAggregate deviceFault(Long deviceId) {
        // 1. 根据ID获取设备聚合根
        Optional<DeviceAggregate> optionalDeviceAggregate = deviceRepository.findById(deviceId);
        if (optionalDeviceAggregate.isEmpty()) {
            throw new IllegalArgumentException("设备不存在：ID=" + deviceId);
        }
        
        // 2. 调用聚合根的故障方法
        DeviceAggregate deviceAggregate = optionalDeviceAggregate.get();
        deviceAggregate.deviceFault();
        
        // 3. 保存更新后的设备聚合根
        return deviceRepository.save(deviceAggregate);
    }
    
    /**
     * 更新设备状态数据
     * @param deviceId 设备ID
     * @param temperature 温度
     * @param humidity 湿度
     * @param voltage 电压
     * @param current 电流
     * @return 更新后的设备聚合根
     */
    public DeviceAggregate updateDeviceStatus(Long deviceId, Float temperature, Float humidity, Float voltage, Float current) {
        // 1. 根据ID获取设备聚合根
        Optional<DeviceAggregate> optionalDeviceAggregate = deviceRepository.findById(deviceId);
        if (optionalDeviceAggregate.isEmpty()) {
            throw new IllegalArgumentException("设备不存在：ID=" + deviceId);
        }
        
        // 2. 调用聚合根的更新状态方法
        DeviceAggregate deviceAggregate = optionalDeviceAggregate.get();
        deviceAggregate.updateDeviceStatus(temperature, humidity, voltage, current);
        
        // 3. 保存更新后的设备聚合根
        return deviceRepository.save(deviceAggregate);
    }
    
    /**
     * 根据ID获取设备
     * @param deviceId 设备ID
     * @return 设备聚合根
     */
    public Optional<DeviceAggregate> getDeviceById(Long deviceId) {
        return deviceRepository.findById(deviceId);
    }
    
    /**
     * 根据设备编码获取设备
     * @param deviceCode 设备编码
     * @return 设备聚合根
     */
    public Optional<DeviceAggregate> getDeviceByCode(String deviceCode) {
        return deviceRepository.findByDeviceCode(deviceCode);
    }
    
    /**
     * 获取所有设备
     * @return 设备聚合根列表
     */
    public List<DeviceAggregate> getAllDevices() {
        return deviceRepository.findAll();
    }
    
    /**
     * 根据设备类型获取设备
     * @param deviceType 设备类型
     * @return 设备聚合根列表
     */
    public List<DeviceAggregate> getDevicesByType(String deviceType) {
        return deviceRepository.findByDeviceType(deviceType);
    }
    
    /**
     * 删除设备
     * @param deviceId 设备ID
     */
    public void deleteDevice(Long deviceId) {
        deviceRepository.deleteById(deviceId);
    }
    
    /**
     * 获取设备总数
     * @return 设备总数
     */
    public long getDeviceCount() {
        return deviceRepository.count();
    }

    /**
     * 获取在线设备数
     */
    public long getOnlineCount() {
        return deviceRepository.countOnline();
    }

    /**
     * 获取故障设备数
     */
    public long getFaultCount() {
        return deviceRepository.countFault();
    }
}