package com.ly.factmesh.iot.application.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ly.factmesh.iot.application.dto.DeviceTelemetryRequest;
import com.ly.factmesh.iot.application.service.DeviceTelemetryService;
import com.ly.factmesh.iot.domain.entity.Device;
import com.ly.factmesh.iot.infrastructure.repository.DeviceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时采集任务
 * 定期将在线设备的 Device 表状态数据（temperature/humidity/voltage/current）同步到 InfluxDB 时序库
 * 适用于边缘/网关按调度上报后更新了 Device 表，或 PATCH /status 更新后需持久化到时序库的场景
 *
 * @author LY-FactMesh
 */
@Component
@ConditionalOnProperty(name = "iot.scheduled-collect.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class ScheduledTelemetryCollectJob {

    private final DeviceMapper deviceMapper;
    private final DeviceTelemetryService deviceTelemetryService;

    @Scheduled(cron = "${iot.scheduled-collect.cron:0 */5 * * * ?}")
    public void collectOnlineDevices() {
        List<Device> onlineDevices = deviceMapper.selectList(
                new LambdaQueryWrapper<Device>().eq(Device::getOnlineStatus, 1));
        if (onlineDevices.isEmpty()) {
            return;
        }
        int count = 0;
        for (Device device : onlineDevices) {
            Map<String, Number> data = buildTelemetryData(device);
            if (data.isEmpty()) {
                continue;
            }
            try {
                DeviceTelemetryRequest req = new DeviceTelemetryRequest();
                req.setDeviceId(device.getId());
                req.setDeviceCode(device.getDeviceCode());
                req.setTimestamp(Instant.now().toEpochMilli());
                req.setData(data);
                deviceTelemetryService.reportTelemetry(req);
                count++;
            } catch (Exception e) {
                log.warn("定时采集设备 {} 遥测失败: {}", device.getDeviceCode(), e.getMessage());
            }
        }
        if (count > 0) {
            log.debug("定时采集完成，同步 {} 台设备遥测到 InfluxDB", count);
        }
    }

    private Map<String, Number> buildTelemetryData(Device device) {
        Map<String, Number> data = new HashMap<>();
        if (device.getTemperature() != null) {
            data.put("temperature", device.getTemperature());
        }
        if (device.getHumidity() != null) {
            data.put("humidity", device.getHumidity());
        }
        if (device.getVoltage() != null) {
            data.put("voltage", device.getVoltage());
        }
        if (device.getCurrent() != null) {
            data.put("current", device.getCurrent());
        }
        return data;
    }
}
