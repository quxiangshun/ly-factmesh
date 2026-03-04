package com.ly.factmesh.iot.application.job;

import com.ly.factmesh.iot.application.dto.DeviceTelemetryRequest;
import com.ly.factmesh.iot.application.service.DeviceApplicationService;
import com.ly.factmesh.iot.application.service.DeviceTelemetryService;
import com.ly.factmesh.iot.domain.aggregate.DeviceAggregate;
import com.ly.factmesh.iot.domain.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模拟器对接采集任务
 * 定期从 mom-simulator REST 拉取 OPC UA / Modbus TCP 模拟点位，上报到 InfluxDB 并更新设备状态
 * 点位映射：Device_n 对应 deviceIds[n-1]，Temperature/Humidity -> temperature/humidity，n_3_201 -> voltage，n_3_200 -> pressure
 *
 * @author LY-FactMesh
 */
@Component
@ConditionalOnProperty(name = "iot.simulator-collect.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class SimulatorCollectorJob {

    private final RestTemplate restTemplate;
    private final DeviceRepository deviceRepository;
    private final DeviceTelemetryService deviceTelemetryService;
    private final DeviceApplicationService deviceApplicationService;

    @org.springframework.beans.factory.annotation.Value("${iot.simulator-collect.url:http://localhost:9089}")
    private String simulatorUrl;

    @Scheduled(cron = "${iot.simulator-collect.cron:0/5 * * * * ?}")
    public void collectFromSimulator() {
        try {
            Map<String, Object> config = restTemplate.exchange(
                    simulatorUrl + "/api/simulator/config",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            ).getBody();
            if (config == null) {
                return;
            }
            @SuppressWarnings("unchecked")
            List<String> deviceIds = (List<String>) config.get("deviceIds");
            if (deviceIds == null || deviceIds.isEmpty()) {
                return;
            }

            Map<String, Object> valuesResp = restTemplate.exchange(
                    simulatorUrl + "/api/simulator/values",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            ).getBody();
            if (valuesResp == null) {
                return;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> opcua = (Map<String, Object>) valuesResp.get("opcua");
            @SuppressWarnings("unchecked")
            Map<String, Object> modbus = (Map<String, Object>) valuesResp.get("modbus");

            for (int i = 0; i < deviceIds.size(); i++) {
                int idx = i + 1;
                Long deviceId = parseDeviceId(deviceIds.get(i));
                if (deviceId == null) {
                    continue;
                }
                var deviceOpt = deviceRepository.findById(deviceId);
                if (deviceOpt.isEmpty()) {
                    continue;
                }
                DeviceAggregate device = deviceOpt.get();
                String deviceCode = device.getDevice().getDeviceCode();

                Map<String, Number> data = new HashMap<>();
                if (opcua != null) {
                    Double temp = getDouble(opcua, "ns=2;s=Device_" + idx + "_Temperature");
                    if (temp != null) data.put("temperature", temp);
                    Double hum = getDouble(opcua, "ns=2;s=Device_" + idx + "_Humidity");
                    if (hum != null) data.put("humidity", hum);
                }
                if (modbus != null) {
                    Number v201 = getNumber(modbus, idx + "_3_201");
                    if (v201 != null) data.put("voltage", v201.floatValue());
                    Number v200 = getNumber(modbus, idx + "_3_200");
                    if (v200 != null) data.put("pressure", v200.floatValue());
                }
                if (data.isEmpty()) {
                    continue;
                }

                try {
                    DeviceTelemetryRequest req = new DeviceTelemetryRequest();
                    req.setDeviceId(deviceId);
                    req.setDeviceCode(deviceCode);
                    req.setTimestamp(Instant.now().toEpochMilli());
                    req.setData(data);
                    deviceTelemetryService.reportTelemetry(req);

                    Float temp = data.containsKey("temperature") ? ((Number) data.get("temperature")).floatValue() : null;
                    Float hum = data.containsKey("humidity") ? ((Number) data.get("humidity")).floatValue() : null;
                    Float voltage = data.containsKey("voltage") ? ((Number) data.get("voltage")).floatValue() : null;
                    deviceApplicationService.updateDeviceStatus(deviceId, temp, hum, voltage, null);
                } catch (Exception e) {
                    log.warn("模拟器采集设备 {} 失败: {}", deviceCode, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.debug("模拟器采集异常: {}", e.getMessage());
        }
    }

    private static Long parseDeviceId(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double getDouble(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number n) return n.doubleValue();
        return null;
    }

    private static Number getNumber(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Number n ? n : null;
    }
}
