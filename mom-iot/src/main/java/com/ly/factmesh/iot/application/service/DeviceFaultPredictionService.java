package com.ly.factmesh.iot.application.service;

import com.ly.factmesh.iot.application.dto.DeviceTelemetryPoint;
import com.ly.factmesh.iot.application.dto.FaultPredictionResult;
import com.ly.factmesh.iot.domain.repository.DeviceRepository;
import com.ly.factmesh.iot.domain.aggregate.DeviceAggregate;
import com.ly.factmesh.iot.domain.entity.Device;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备故障预测服务
 * 基于遥测数据统计分析与告警历史，评估设备故障风险（无 ML 依赖，纯统计算法）
 *
 * @author LY-FactMesh
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceFaultPredictionService {

    private static final String[] TELEMETRY_FIELDS = {"temperature", "temp", "humidity", "hum", "voltage", "volt", "current", "curr"};
    private static final double Z_SCORE_THRESHOLD = 2.0;
    private static final int MIN_POINTS_FOR_ANALYSIS = 10;
    private static final int MAX_HOURS = 168; // 7 days

    private final DeviceTelemetryService deviceTelemetryService;
    private final DeviceAlertService deviceAlertService;
    private final DeviceRepository deviceRepository;

    /**
     * 预测设备故障风险
     *
     * @param deviceId 设备ID
     * @param hours    分析时间窗口（小时），默认 24
     * @return 预测结果
     */
    public FaultPredictionResult predict(Long deviceId, int hours) {
        hours = Math.min(Math.max(hours, 1), MAX_HOURS);
        Instant end = Instant.now();
        Instant start = end.minus(hours, ChronoUnit.HOURS);

        Optional<DeviceAggregate> opt = deviceRepository.findById(deviceId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("设备不存在: " + deviceId);
        }
        Device device = opt.get().getDevice();
        device.rebuildStatusFromFields();

        List<String> factors = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        double riskScore = 0;

        // 1. 设备已故障 → 直接高分
        if (device.getRunningStatus() != null && device.getRunningStatus() == 2) {
            riskScore = 95;
            factors.add("设备当前处于故障状态");
            recommendations.add("立即排查故障原因并修复");
            return buildResult(deviceId, device, hours, riskScore, factors, recommendations, true);
        }

        // 2. 遥测数据异常检测（温度、湿度、电压、电流）
        Map<String, List<Double>> fieldValues = fetchTelemetryByFields(deviceId, start, end);
        boolean dataSufficient = fieldValues.values().stream()
                .anyMatch(list -> list.size() >= MIN_POINTS_FOR_ANALYSIS);

        for (Map.Entry<String, List<Double>> entry : fieldValues.entrySet()) {
            String field = entry.getKey();
            List<Double> values = entry.getValue();
            if (values.size() < MIN_POINTS_FOR_ANALYSIS) continue;

            double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double variance = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0);
            double std = Math.sqrt(variance);
            double latest = values.get(values.size() - 1);

            if (std > 1e-9) {
                double zScore = Math.abs((latest - mean) / std);
                if (zScore > Z_SCORE_THRESHOLD) {
                    riskScore += 15;
                    factors.add(String.format("测点 %s 当前值 %.2f 偏离均值 %.2f 约 %.1f 倍标准差", field, latest, mean, zScore));
                }
            }

            // 趋势：后 20% 均值 vs 前 20% 均值（温度上升、电压下降等）
            int n = values.size();
            int slice = Math.max(1, n / 5);
            double headAvg = values.subList(0, slice).stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double tailAvg = values.subList(n - slice, n).stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double trend = tailAvg - headAvg;
            if (field.contains("temp") || field.contains("temperature")) {
                if (trend > 5) {
                    riskScore += 10;
                    factors.add(String.format("温度呈上升趋势（前段均值 %.1f → 后段 %.1f）", headAvg, tailAvg));
                }
            } else if (field.contains("volt") || field.contains("voltage")) {
                if (trend < -5) {
                    riskScore += 12;
                    factors.add(String.format("电压呈下降趋势（前段均值 %.1f → 后段 %.1f）", headAvg, tailAvg));
                }
            }
        }

        // 3. 近期告警次数
        LocalDateTime since = LocalDateTime.ofInstant(start, ZoneId.systemDefault());
        long alertCount = deviceAlertService.countRecentByDevice(deviceId, since);
        if (alertCount > 0) {
            riskScore += Math.min(alertCount * 8, 30);
            factors.add(String.format("近 %d 小时内发生 %d 次告警", hours, alertCount));
            recommendations.add("建议结合告警记录排查设备工况");
        }

        // 4. 数据不足时的提示
        if (!dataSufficient && riskScore < 30) {
            factors.add("遥测数据不足，无法进行充分分析");
            recommendations.add("确保设备持续上报遥测数据以提升预测准确度");
        }

        if (riskScore < 20 && factors.isEmpty()) {
            factors.add("当前数据未见明显异常");
        }
        if (riskScore >= 50 && recommendations.isEmpty()) {
            recommendations.add("建议安排巡检或预防性维护");
        }
        if (riskScore >= 70) {
            recommendations.add("建议尽快安排设备检修");
        }

        riskScore = Math.min(100, riskScore);
        return buildResult(deviceId, device, hours, riskScore, factors, recommendations, dataSufficient);
    }

    private Map<String, List<Double>> fetchTelemetryByFields(Long deviceId, Instant start, Instant end) {
        Map<String, List<Double>> result = new HashMap<>();
        for (String field : TELEMETRY_FIELDS) {
            List<DeviceTelemetryPoint> points = deviceTelemetryService.queryTelemetry(deviceId, field, start, end, 2000);
            List<Double> values = points.stream()
                    .map(DeviceTelemetryPoint::getValue)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!values.isEmpty()) {
                result.put(field, values);
            }
        }
        return result;
    }

    private FaultPredictionResult buildResult(Long deviceId, Device device, int hours,
            double riskScore, List<String> factors, List<String> recommendations, boolean dataSufficient) {
        String riskLevel;
        if (riskScore >= 80) riskLevel = "CRITICAL";
        else if (riskScore >= 50) riskLevel = "HIGH";
        else if (riskScore >= 20) riskLevel = "MEDIUM";
        else riskLevel = "LOW";

        return FaultPredictionResult.builder()
                .deviceId(deviceId)
                .deviceCode(device.getDeviceCode())
                .deviceName(device.getDeviceName())
                .riskLevel(riskLevel)
                .riskScore((int) Math.round(riskScore))
                .analysisHours(hours)
                .factors(factors)
                .recommendations(recommendations)
                .dataSufficient(dataSufficient)
                .build();
    }
}
