package com.ly.factmesh.iot.domain.event;

import com.ly.factmesh.iot.domain.aggregate.DeviceAggregate;

import java.util.UUID;

/**
 * 设备注册事件
 * 当设备成功注册时发布此事件
 *
 * @author 屈想顺
 */
public class DeviceRegisteredEvent extends DomainEvent {
    
    /** 设备聚合根 */
    private final DeviceAggregate deviceAggregate;
    
    /**
     * 构造函数
     * @param deviceAggregate 设备聚合根
     */
    public DeviceRegisteredEvent(DeviceAggregate deviceAggregate) {
        super(UUID.randomUUID().toString(), "DeviceRegistered");
        this.deviceAggregate = deviceAggregate;
    }
    
    /**
     * 获取设备聚合根
     * @return 设备聚合根
     */
    public DeviceAggregate getDeviceAggregate() {
        return deviceAggregate;
    }
}