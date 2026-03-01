package com.ly.factmesh.iot.domain.aggregate;

import com.ly.factmesh.iot.domain.entity.Device;
import com.ly.factmesh.iot.domain.event.DeviceRegisteredEvent;
import com.ly.factmesh.iot.domain.event.DeviceStatusChangedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备聚合根
 * 以设备实体为中心的聚合，管理设备相关的所有对象
 * 是DDD中边界控制的核心对象
 *
 * @author 屈想顺
 */
public class DeviceAggregate {
    
    /** 设备实体（聚合根） */
    private Device device;
    
    /** 未发布的领域事件列表 */
    private final List<Object> domainEvents = new ArrayList<>();
    
    // 构造函数
    public DeviceAggregate() {
        this.device = new Device();
    }
    
    public DeviceAggregate(Device device) {
        this.device = device;
    }
    
    // 业务方法（聚合级别的业务逻辑）
    /**
     * 注册设备
     */
    public void registerDevice() {
        // 注册设备的业务逻辑
        // 可以包括设备编码验证、状态初始化等
        device.online();
        
        // 发布设备注册事件
        this.domainEvents.add(new DeviceRegisteredEvent(this));
    }
    
    /**
     * 更新设备信息
     */
    public void updateDeviceInfo(String deviceName, String deviceType, String model, String manufacturer, String installLocation) {
        device.setDeviceName(deviceName);
        device.setDeviceType(deviceType);
        device.setModel(model);
        device.setManufacturer(manufacturer);
        device.setInstallLocation(installLocation);
    }
    
    /**
     * 设备上线
     */
    public void deviceOnline() {
        int oldOnlineStatus = device.getStatus().getOnlineStatus();
        int oldRunningStatus = device.getStatus().getRunningStatus();
        
        device.online();
        
        // 如果状态发生变化，发布设备状态变更事件
        if (oldOnlineStatus != device.getStatus().getOnlineStatus() || 
            oldRunningStatus != device.getStatus().getRunningStatus()) {
            this.domainEvents.add(new DeviceStatusChangedEvent(device));
        }
    }
    
    /**
     * 设备离线
     */
    public void deviceOffline() {
        int oldOnlineStatus = device.getStatus().getOnlineStatus();
        int oldRunningStatus = device.getStatus().getRunningStatus();
        
        device.offline();
        
        // 如果状态发生变化，发布设备状态变更事件
        if (oldOnlineStatus != device.getStatus().getOnlineStatus() || 
            oldRunningStatus != device.getStatus().getRunningStatus()) {
            this.domainEvents.add(new DeviceStatusChangedEvent(device));
        }
    }
    
    /**
     * 设备开始运行
     */
    public void startRunning() {
        int oldOnlineStatus = device.getStatus().getOnlineStatus();
        int oldRunningStatus = device.getStatus().getRunningStatus();
        
        device.startRunning();
        
        // 如果状态发生变化，发布设备状态变更事件
        if (oldOnlineStatus != device.getStatus().getOnlineStatus() || 
            oldRunningStatus != device.getStatus().getRunningStatus()) {
            this.domainEvents.add(new DeviceStatusChangedEvent(device));
        }
    }
    
    /**
     * 设备停止运行
     */
    public void stopRunning() {
        int oldOnlineStatus = device.getStatus().getOnlineStatus();
        int oldRunningStatus = device.getStatus().getRunningStatus();
        
        device.stopRunning();
        
        // 如果状态发生变化，发布设备状态变更事件
        if (oldOnlineStatus != device.getStatus().getOnlineStatus() || 
            oldRunningStatus != device.getStatus().getRunningStatus()) {
            this.domainEvents.add(new DeviceStatusChangedEvent(device));
        }
    }
    
    /**
     * 设备故障
     */
    public void deviceFault() {
        int oldOnlineStatus = device.getStatus().getOnlineStatus();
        int oldRunningStatus = device.getStatus().getRunningStatus();
        
        device.fault();
        
        // 如果状态发生变化，发布设备状态变更事件
        if (oldOnlineStatus != device.getStatus().getOnlineStatus() || 
            oldRunningStatus != device.getStatus().getRunningStatus()) {
            this.domainEvents.add(new DeviceStatusChangedEvent(device));
        }
    }
    
    /**
     * 更新设备状态数据
     */
    public void updateDeviceStatus(Float temperature, Float humidity, Float voltage, Float current) {
        device.updateStatus(temperature, humidity, voltage, current);
        
        // 发布设备状态变更事件
        this.domainEvents.add(new DeviceStatusChangedEvent(device));
    }
    
    /**
     * 获取所有未发布的领域事件
     * @return 领域事件列表
     */
    public List<Object> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }
    
    /**
     * 清除所有已发布的领域事件
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    // Getters and Setters
    public Device getDevice() {
        return device;
    }
    
    public void setDevice(Device device) {
        this.device = device;
    }
    
    public Long getId() {
        return device.getId();
    }
    
    public String getDeviceCode() {
        return device.getDeviceCode();
    }
    
    public void setDeviceCode(String deviceCode) {
        device.setDeviceCode(deviceCode);
    }
    
    public String getDeviceName() {
        return device.getDeviceName();
    }
    
    public void setDeviceName(String deviceName) {
        device.setDeviceName(deviceName);
    }
}
