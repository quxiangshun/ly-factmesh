package com.ly.factmesh.iot.domain.valueobject;

import java.time.LocalDateTime;

/**
 * 设备状态值对象
 * 表示设备的当前状态信息，无唯一标识，是设备实体的组成部分
 *
 * @author 屈想顺
 */
public class DeviceStatus {
    
    /** 在线状态：0-离线，1-在线 */
    private int onlineStatus;
    
    /** 运行状态：0-停止，1-运行，2-故障 */
    private int runningStatus;
    
    /** 温度 */
    private Float temperature;
    
    /** 湿度 */
    private Float humidity;
    
    /** 电压 */
    private Float voltage;
    
    /** 电流 */
    private Float current;
    
    /** 最后更新时间 */
    private LocalDateTime lastUpdateTime;
    
    // 构造函数
    public DeviceStatus() {
        this.lastUpdateTime = LocalDateTime.now();
    }
    
    public DeviceStatus(int onlineStatus, int runningStatus) {
        this.onlineStatus = onlineStatus;
        this.runningStatus = runningStatus;
        this.lastUpdateTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getOnlineStatus() {
        return onlineStatus;
    }
    
    public void setOnlineStatus(int onlineStatus) {
        this.onlineStatus = onlineStatus;
        this.lastUpdateTime = LocalDateTime.now();
    }
    
    public int getRunningStatus() {
        return runningStatus;
    }
    
    public void setRunningStatus(int runningStatus) {
        this.runningStatus = runningStatus;
        this.lastUpdateTime = LocalDateTime.now();
    }
    
    public Float getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Float temperature) {
        this.temperature = temperature;
        this.lastUpdateTime = LocalDateTime.now();
    }
    
    public Float getHumidity() {
        return humidity;
    }
    
    public void setHumidity(Float humidity) {
        this.humidity = humidity;
        this.lastUpdateTime = LocalDateTime.now();
    }
    
    public Float getVoltage() {
        return voltage;
    }
    
    public void setVoltage(Float voltage) {
        this.voltage = voltage;
        this.lastUpdateTime = LocalDateTime.now();
    }
    
    public Float getCurrent() {
        return current;
    }
    
    public void setCurrent(Float current) {
        this.current = current;
        this.lastUpdateTime = LocalDateTime.now();
    }
    
    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
    
    // 业务方法
    public boolean isOnline() {
        return this.onlineStatus == 1;
    }
    
    public boolean isRunning() {
        return this.runningStatus == 1;
    }
    
    public boolean isFault() {
        return this.runningStatus == 2;
    }
}
