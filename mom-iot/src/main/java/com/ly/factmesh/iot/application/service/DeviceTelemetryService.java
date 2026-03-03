package com.ly.factmesh.iot.application.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.ly.factmesh.iot.application.dto.DeviceTelemetryPoint;
import com.ly.factmesh.iot.application.dto.DeviceTelemetryRequest;
import com.ly.factmesh.iot.domain.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备遥测数据服务
 * 将采集数据写入 InfluxDB 时序库，支持查询历史数据
 *
 * @author LY-FactMesh
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceTelemetryService {

    private static final String MEASUREMENT = "device_telemetry";

    private final InfluxDBClient influxDBClient;
    private final DeviceRepository deviceRepository;
    private final AlertRuleEngineService alertRuleEngineService;

    @Value("${influxdb.bucket:iot-telemetry}")
    private String bucket;

    @Value("${influxdb.org:ly-factmesh}")
    private String influxOrg;

    private final TelemetryDataCleaner telemetryDataCleaner;

    /**
     * 上报设备遥测数据（经数据清洗后写入）
     */
    public void reportTelemetry(DeviceTelemetryRequest request) {
        if (request.getData() == null || request.getData().isEmpty()) {
            return;
        }
        Map<String, Number> cleaned = telemetryDataCleaner.clean(request.getData());
        if (cleaned.isEmpty()) {
            log.debug("设备 {} 遥测数据经清洗后无有效数据，跳过写入", request.getDeviceId());
            return;
        }

        var device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + request.getDeviceId()));
        String deviceCode = request.getDeviceCode() != null ? request.getDeviceCode() : device.getDeviceCode();

        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        Instant time = request.getCollectTime();

        for (Map.Entry<String, Number> entry : cleaned.entrySet()) {
            Point point = Point.measurement(MEASUREMENT)
                    .addTag("device_id", String.valueOf(request.getDeviceId()))
                    .addTag("device_code", deviceCode != null ? deviceCode : "")
                    .addTag("field", entry.getKey())
                    .addField("value", entry.getValue().doubleValue())
                    .time(time, WritePrecision.MS);
            writeApi.writePoint(bucket, influxOrg, point);
        }
        log.debug("设备 {} 遥测数据已写入 InfluxDB", request.getDeviceId());

        // 根据规则引擎评估并触发自动告警
        try {
            alertRuleEngineService.evaluate(request.getDeviceId(), deviceCode, cleaned);
        } catch (Exception e) {
            log.warn("告警规则评估异常: {}", e.getMessage());
        }
    }

    /**
     * 查询设备遥测历史数据（Flux 查询）
     */
    public List<DeviceTelemetryPoint> queryTelemetry(Long deviceId, String field, Instant start, Instant end, Integer limit) {
        if (deviceId == null) {
            return List.of();
        }
        String deviceIdStr = String.valueOf(deviceId);
        String fieldFilter = field != null && !field.isBlank() ? " and r[\"field\"] == \"" + field.replace("\"", "\\\"") + "\"" : "";
        String startStr = start != null ? "time(v: \"" + start + "\")" : "-30d";
        String endStr = end != null ? "time(v: \"" + end + "\")" : "now()";
        int limitVal = limit != null && limit > 0 ? Math.min(limit, 10000) : 1000;

        String flux = String.format(
                "from(bucket: \"%s\") " +
                "|> range(start: %s, stop: %s) " +
                "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\" and r[\"device_id\"] == \"%s\"%s) " +
                "|> sort(columns: [\"_time\"], desc: true) " +
                "|> limit(n: %d)",
                bucket, startStr, endStr, MEASUREMENT, deviceIdStr, fieldFilter, limitVal);

        List<DeviceTelemetryPoint> result = new ArrayList<>();
        try {
            influxDBClient.getQueryApi().query(flux).forEach(table -> table.getRecords().forEach(record -> {
                Object value = record.getValue();
                if (value instanceof Number) {
                    result.add(DeviceTelemetryPoint.builder()
                            .deviceId(deviceId)
                            .deviceCode(record.getValueByKey("device_code") != null ? record.getValueByKey("device_code").toString() : null)
                            .field(record.getField())
                            .value(((Number) value).doubleValue())
                            .time(record.getTime())
                            .build());
                }
            }));
        } catch (Exception e) {
            log.warn("查询遥测数据失败: {}", e.getMessage());
        }
        return result;
    }
}
