package com.ly.factmesh.iot.application.dto;

import java.time.LocalDateTime;

/**
 * 设备数据传输对象
 * 用于在应用层和表示层之间传递设备数据
 *
 * @author 屈想顺
 */
public class DeviceDTO {
    
    /** 设备ID */
    private Long id;
    
    /** 设备编码 */
    private String deviceCode;
    
    /** 设备名称 */
    private String deviceName;
    
    /** 设备类型 */
    private String deviceType;
    
    /** 设备型号 */
    private String model;
    
    /** 制造商 */
    private String manufacturer;
    
    /** 安装位置 */
    private String installLocation;
    
    /** 在线状态 */
    private int onlineStatus;
    
    /** 运行状态 */
    private int runningStatus;
    
    /** 温度 */
    private Float temperature;
    
    /** 湿度 */
    private Float humidity;
    
    /** 电压 */
    private Float voltage;
    
    /** 电流 */
    private Float current;
    
    /** 状态最后更新时间 */
    private LocalDateTime statusLastUpdateTime;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDeviceCode() {
        return deviceCode;
    }
    
    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    public String getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getManufacturer() {
        return manufacturer;
    }
    
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    public String getInstallLocation() {
        return installLocation;
    }
    
    public void setInstallLocation(String installLocation) {
        this.installLocation = installLocation;
    }
    
    public int getOnlineStatus() {
        return onlineStatus;
    }
    
    public void setOnlineStatus(int onlineStatus) {
        this.onlineStatus = onlineStatus;
    }
    
    public int getRunningStatus() {
        return runningStatus;
    }
    
    public void setRunningStatus(int runningStatus) {
        this.runningStatus = runningStatus;
    }
    
    public Float getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }
    
    public Float getHumidity() {
        return humidity;
    }
    
    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }
    
    public Float getVoltage() {
        return voltage;
    }
    
    public void setVoltage(Float voltage) {
        this.voltage = voltage;
    }
    
    public Float getCurrent() {
        return current;
    }
    
    public void setCurrent(Float current) {
        this.current = current;
    }
    
    public LocalDateTime getStatusLastUpdateTime() {
        return statusLastUpdateTime;
    }
    
    public void setStatusLastUpdateTime(LocalDateTime statusLastUpdateTime) {
        this.statusLastUpdateTime = statusLastUpdateTime;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}