package com.ly.factmesh.iot.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ly.factmesh.iot.domain.valueobject.DeviceStatus;

import java.time.LocalDateTime;

/**
 * 设备实体
 * 表示物理设备，具有唯一标识，是核心领域对象
 *
 * @author 屈想顺
 */
@TableName("device")
public class Device {
    
    /** 设备ID（雪花ID） */
    @TableId("id")
    private Long id;
    
    /** 设备编码 */
    @TableField("device_code")
    private String deviceCode;
    
    /** 设备名称 */
    @TableField("device_name")
    private String deviceName;
    
    /** 设备类型 */
    @TableField("device_type")
    private String deviceType;
    
    /** 设备型号 */
    @TableField("model")
    private String model;
    
    /** 制造商 */
    @TableField("manufacturer")
    private String manufacturer;
    
    /** 安装位置 */
    @TableField("install_location")
    private String installLocation;
    
    /** 在线状态：0-离线，1-在线 */
    @TableField("online_status")
    private Integer onlineStatus;
    
    /** 运行状态：0-停止，1-运行，2-故障 */
    @TableField("running_status")
    private Integer runningStatus;
    
    /** 温度 */
    @TableField("temperature")
    private Float temperature;
    
    /** 湿度 */
    @TableField("humidity")
    private Float humidity;
    
    /** 电压 */
    @TableField("voltage")
    private Float voltage;
    
    /** 电流 */
    @TableField("current")
    private Float current;
    
    /** 状态最后更新时间 */
    @TableField("status_last_update_time")
    private LocalDateTime statusLastUpdateTime;
    
    /** 设备状态（值对象，由上述字段构建，不持久化） */
    @TableField(exist = false)
    private DeviceStatus status;
    
    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;
    
    /** 更新时间 */
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    // 构造函数
    public Device() {
        this.onlineStatus = 0;
        this.runningStatus = 0;
        this.status = new DeviceStatus();
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    public Device(String deviceCode, String deviceName) {
        this.deviceCode = deviceCode;
        this.deviceName = deviceName;
        this.onlineStatus = 0;
        this.runningStatus = 0;
        this.status = new DeviceStatus();
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    /** 同步 status 值对象与实体字段 */
    private void syncStatusToFields() {
        if (status != null) {
            this.onlineStatus = status.getOnlineStatus();
            this.runningStatus = status.getRunningStatus();
            this.temperature = status.getTemperature();
            this.humidity = status.getHumidity();
            this.voltage = status.getVoltage();
            this.current = status.getCurrent();
            this.statusLastUpdateTime = status.getLastUpdateTime();
        }
    }
    
    /** 从实体字段构建 status 值对象（加载后调用） */
    public void rebuildStatusFromFields() {
        if (status == null) {
            status = new DeviceStatus();
        }
        status.setOnlineStatus(onlineStatus != null ? onlineStatus : 0);
        status.setRunningStatus(runningStatus != null ? runningStatus : 0);
        status.setTemperature(temperature);
        status.setHumidity(humidity);
        status.setVoltage(voltage);
        status.setCurrent(current);
        status.setLastUpdateTime(statusLastUpdateTime != null ? statusLastUpdateTime : LocalDateTime.now());
    }
    
    // 业务方法
    /**
     * 设备上线
     */
    public void online() {
        this.status.setOnlineStatus(1);
        this.status.setRunningStatus(0);
        this.updateTime = LocalDateTime.now();
        syncStatusToFields();
    }
    
    /**
     * 设备离线
     */
    public void offline() {
        this.status.setOnlineStatus(0);
        this.status.setRunningStatus(0);
        this.updateTime = LocalDateTime.now();
        syncStatusToFields();
    }
    
    /**
     * 设备开始运行
     */
    public void startRunning() {
        if ((this.status != null && this.status.getOnlineStatus() == 1) || (onlineStatus != null && onlineStatus == 1)) {
            if (status == null) rebuildStatusFromFields();
            this.status.setRunningStatus(1);
            this.updateTime = LocalDateTime.now();
            syncStatusToFields();
        } else {
            throw new IllegalStateException("设备未上线，无法开始运行");
        }
    }
    
    /**
     * 设备停止运行
     */
    public void stopRunning() {
        if (status == null) rebuildStatusFromFields();
        this.status.setRunningStatus(0);
        this.updateTime = LocalDateTime.now();
        syncStatusToFields();
    }
    
    /**
     * 设备故障
     */
    public void fault() {
        if (status == null) rebuildStatusFromFields();
        this.status.setRunningStatus(2);
        this.updateTime = LocalDateTime.now();
        syncStatusToFields();
    }
    
    /**
     * 更新设备状态数据
     */
    public void updateStatus(Float temperature, Float humidity, Float voltage, Float current) {
        if (status == null) rebuildStatusFromFields();
        if (temperature != null) this.status.setTemperature(temperature);
        if (humidity != null) this.status.setHumidity(humidity);
        if (voltage != null) this.status.setVoltage(voltage);
        if (current != null) this.status.setCurrent(current);
        this.updateTime = LocalDateTime.now();
        syncStatusToFields();
    }
    
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
    
    public DeviceStatus getStatus() {
        return status;
    }
    
    public void setStatus(DeviceStatus status) {
        this.status = status;
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
    
    public Integer getOnlineStatus() { return onlineStatus; }
    public void setOnlineStatus(Integer onlineStatus) { this.onlineStatus = onlineStatus; }
    public Integer getRunningStatus() { return runningStatus; }
    public void setRunningStatus(Integer runningStatus) { this.runningStatus = runningStatus; }
    public Float getTemperature() { return temperature; }
    public void setTemperature(Float temperature) { this.temperature = temperature; }
    public Float getHumidity() { return humidity; }
    public void setHumidity(Float humidity) { this.humidity = humidity; }
    public Float getVoltage() { return voltage; }
    public void setVoltage(Float voltage) { this.voltage = voltage; }
    public Float getCurrent() { return current; }
    public void setCurrent(Float current) { this.current = current; }
    public LocalDateTime getStatusLastUpdateTime() { return statusLastUpdateTime; }
    public void setStatusLastUpdateTime(LocalDateTime statusLastUpdateTime) { this.statusLastUpdateTime = statusLastUpdateTime; }
}
