package com.ly.factmesh.iot.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认遥测数据清洗实现
 * - 过滤 null、NaN、Infinity
 * - 可选范围校验（temperature、humidity、voltage、current）
 *
 * @author LY-FactMesh
 */
@Component
@ConditionalOnProperty(name = "iot.telemetry.cleaner.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class DefaultTelemetryDataCleaner implements TelemetryDataCleaner {

    private static final double TEMP_MIN = -50;
    private static final double TEMP_MAX = 200;
    private static final double HUMIDITY_MIN = 0;
    private static final double HUMIDITY_MAX = 100;
    private static final double VOLTAGE_MIN = 0;
    private static final double VOLTAGE_MAX = 1000;
    private static final double CURRENT_MIN = 0;
    private static final double CURRENT_MAX = 10000;

    @Override
    public Map<String, Number> clean(Map<String, Number> raw) {
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }
        Map<String, Number> result = new HashMap<>();
        for (Map.Entry<String, Number> entry : raw.entrySet()) {
            Number v = entry.getValue();
            if (v == null || !isValidNumber(v.doubleValue())) {
                log.debug("过滤无效遥测: {} = {}", entry.getKey(), v);
                continue;
            }
            double d = v.doubleValue();
            String key = entry.getKey();
            if (!isInRange(key, d)) {
                log.debug("过滤超范围遥测: {} = {} (超出合理范围)", key, d);
                continue;
            }
            result.put(key, v);
        }
        return result;
    }

    private boolean isValidNumber(double d) {
        return !Double.isNaN(d) && !Double.isInfinite(d);
    }

    private boolean isInRange(String field, double value) {
        return switch (field.toLowerCase()) {
            case "temperature", "temp" -> value >= TEMP_MIN && value <= TEMP_MAX;
            case "humidity", "hum" -> value >= HUMIDITY_MIN && value <= HUMIDITY_MAX;
            case "voltage", "volt" -> value >= VOLTAGE_MIN && value <= VOLTAGE_MAX;
            case "current", "curr" -> value >= CURRENT_MIN && value <= CURRENT_MAX;
            default -> true;
        };
    }
}
