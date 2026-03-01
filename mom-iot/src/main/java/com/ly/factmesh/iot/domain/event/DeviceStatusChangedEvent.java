package com.ly.factmesh.iot.domain.event;

import com.ly.factmesh.iot.domain.entity.Device;

import java.util.UUID;

/**
 * 设备状态变更事件
 * 当设备状态发生变化时发布此事件
 *
 * @author 屈想顺
 */
public class DeviceStatusChangedEvent extends DomainEvent {
    
    /** 设备ID */
    private final Long deviceId;
    
    /** 设备编码 */
    private final String deviceCode;
    
    /** 在线状态 */
    private final int onlineStatus;
    
    /** 运行状态 */
    private final int runningStatus;
    
    /**
     * 构造函数
     * @param device 设备实体
     */
    public DeviceStatusChangedEvent(Device device) {
        super(UUID.randomUUID().toString(), "DeviceStatusChanged");
        this.deviceId = device.getId();
        this.deviceCode = device.getDeviceCode();
        this.onlineStatus = device.getStatus().getOnlineStatus();
        this.runningStatus = device.getStatus().getRunningStatus();
    }
    
    /**
     * 获取设备ID
     * @return 设备ID
     */
    public Long getDeviceId() {
        return deviceId;
    }
    
    /**
     * 获取设备编码
     * @return 设备编码
     */
    public String getDeviceCode() {
        return deviceCode;
    }
    
    /**
     * 获取在线状态
     * @return 在线状态
     */
    public int getOnlineStatus() {
        return onlineStatus;
    }
    
    /**
     * 获取运行状态
     * @return 运行状态
     */
    public int getRunningStatus() {
        return runningStatus;
    }
}