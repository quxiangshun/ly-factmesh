package com.ly.factmesh.iot.application.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * 遥测数据清洗空实现（清洗功能关闭时使用）
 *
 * @author LY-FactMesh
 */
@Component
@ConditionalOnProperty(name = "iot.telemetry.cleaner.enabled", havingValue = "false")
public class NoOpTelemetryDataCleaner implements TelemetryDataCleaner {

    @Override
    public Map<String, Number> clean(Map<String, Number> raw) {
        return raw != null ? raw : Collections.emptyMap();
    }
}
