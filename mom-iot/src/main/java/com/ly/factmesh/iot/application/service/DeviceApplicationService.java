package com.ly.factmesh.iot.application.service;

import com.ly.factmesh.iot.application.dto.DeviceDTO;
import com.ly.factmesh.iot.domain.aggregate.DeviceAggregate;
import com.ly.factmesh.iot.domain.service.DeviceDomainService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 设备应用服务
 * 协调领域服务和基础设施，为表示层提供设备相关功能
 *
 * @author 屈想顺
 */
@Service
@RequiredArgsConstructor
public class DeviceApplicationService {
    
    private final DeviceDomainService deviceDomainService;
    
    /**
     * 将设备聚合根转换为设备DTO
     * @param deviceAggregate 设备聚合根
     * @return 设备DTO
     */
    private DeviceDTO toDTO(DeviceAggregate deviceAggregate) {
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setId(deviceAggregate.getId());
        deviceDTO.setDeviceCode(deviceAggregate.getDevice().getDeviceCode());
        deviceDTO.setDeviceName(deviceAggregate.getDevice().getDeviceName());
        deviceDTO.setDeviceType(deviceAggregate.getDevice().getDeviceType());
        deviceDTO.setModel(deviceAggregate.getDevice().getModel());
        deviceDTO.setManufacturer(deviceAggregate.getDevice().getManufacturer());
        deviceDTO.setInstallLocation(deviceAggregate.getDevice().getInstallLocation());
        deviceDTO.setOnlineStatus(deviceAggregate.getDevice().getStatus().getOnlineStatus());
        deviceDTO.setRunningStatus(deviceAggregate.getDevice().getStatus().getRunningStatus());
        deviceDTO.setTemperature(deviceAggregate.getDevice().getStatus().getTemperature());
        deviceDTO.setHumidity(deviceAggregate.getDevice().getStatus().getHumidity());
        deviceDTO.setVoltage(deviceAggregate.getDevice().getStatus().getVoltage());
        deviceDTO.setCurrent(deviceAggregate.getDevice().getStatus().getCurrent());
        deviceDTO.setStatusLastUpdateTime(deviceAggregate.getDevice().getStatus().getLastUpdateTime());
        deviceDTO.setCreateTime(deviceAggregate.getDevice().getCreateTime());
        deviceDTO.setUpdateTime(deviceAggregate.getDevice().getUpdateTime());
        return deviceDTO;
    }
    
    /**
     * 注册新设备
     * @param deviceCode 设备编码
     * @param deviceName 设备名称
     * @param deviceType 设备类型
     * @param model 设备型号
     * @param manufacturer 制造商
     * @param installLocation 安装位置
     * @return 设备DTO
     */
    public DeviceDTO registerDevice(String deviceCode, String deviceName, String deviceType, String model, String manufacturer, String installLocation) {
        DeviceAggregate deviceAggregate = deviceDomainService.registerDevice(deviceCode, deviceName, deviceType, model, manufacturer, installLocation);
        return toDTO(deviceAggregate);
    }
    
    /**
     * 更新设备信息
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     * @param deviceType 设备类型
     * @param model 设备型号
     * @param manufacturer 制造商
     * @param installLocation 安装位置
     * @return 设备DTO
     */
    public DeviceDTO updateDeviceInfo(Long deviceId, String deviceName, String deviceType, String model, String manufacturer, String installLocation) {
        DeviceAggregate deviceAggregate = deviceDomainService.updateDeviceInfo(deviceId, deviceName, deviceType, model, manufacturer, installLocation);
        return toDTO(deviceAggregate);
    }
    
    /**
     * 设备上线
     * @param deviceId 设备ID
     * @return 设备DTO
     */
    public DeviceDTO deviceOnline(Long deviceId) {
        DeviceAggregate deviceAggregate = deviceDomainService.deviceOnline(deviceId);
        return toDTO(deviceAggregate);
    }
    
    /**
     * 设备离线
     * @param deviceId 设备ID
     * @return 设备DTO
     */
    public DeviceDTO deviceOffline(Long deviceId) {
        DeviceAggregate deviceAggregate = deviceDomainService.deviceOffline(deviceId);
        return toDTO(deviceAggregate);
    }
    
    /**
     * 设备开始运行
     * @param deviceId 设备ID
     * @return 设备DTO
     */
    public DeviceDTO startRunning(Long deviceId) {
        DeviceAggregate deviceAggregate = deviceDomainService.startRunning(deviceId);
        return toDTO(deviceAggregate);
    }
    
    /**
     * 设备停止运行
     * @param deviceId 设备ID
     * @return 设备DTO
     */
    public DeviceDTO stopRunning(Long deviceId) {
        DeviceAggregate deviceAggregate = deviceDomainService.stopRunning(deviceId);
        return toDTO(deviceAggregate);
    }
    
    /**
     * 设备故障
     * @param deviceId 设备ID
     * @return 设备DTO
     */
    public DeviceDTO deviceFault(Long deviceId) {
        DeviceAggregate deviceAggregate = deviceDomainService.deviceFault(deviceId);
        return toDTO(deviceAggregate);
    }
    
    /**
     * 更新设备状态数据
     * @param deviceId 设备ID
     * @param temperature 温度
     * @param humidity 湿度
     * @param voltage 电压
     * @param current 电流
     * @return 设备DTO
     */
    public DeviceDTO updateDeviceStatus(Long deviceId, Float temperature, Float humidity, Float voltage, Float current) {
        DeviceAggregate deviceAggregate = deviceDomainService.updateDeviceStatus(deviceId, temperature, humidity, voltage, current);
        return toDTO(deviceAggregate);
    }
    
    /**
     * 根据ID获取设备
     * @param deviceId 设备ID
     * @return 设备DTO
     */
    public Optional<DeviceDTO> getDeviceById(Long deviceId) {
        return deviceDomainService.getDeviceById(deviceId)
                .map(this::toDTO);
    }
    
    /**
     * 根据设备编码获取设备
     * @param deviceCode 设备编码
     * @return 设备DTO
     */
    public Optional<DeviceDTO> getDeviceByCode(String deviceCode) {
        return deviceDomainService.getDeviceByCode(deviceCode)
                .map(this::toDTO);
    }
    
    /**
     * 获取所有设备
     * @return 设备DTO列表
     */
    public List<DeviceDTO> getAllDevices() {
        return deviceDomainService.getAllDevices().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据设备类型获取设备
     * @param deviceType 设备类型
     * @return 设备DTO列表
     */
    public List<DeviceDTO> getDevicesByType(String deviceType) {
        return deviceDomainService.getDevicesByType(deviceType).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 删除设备
     * @param deviceId 设备ID
     */
    public void deleteDevice(Long deviceId) {
        deviceDomainService.deleteDevice(deviceId);
    }
    
    /**
     * 获取设备总数
     * @return 设备总数
     */
    public long getDeviceCount() {
        return deviceDomainService.getDeviceCount();
    }
}