package com.ly.factmesh.iot.application.service;

import com.ly.factmesh.iot.domain.entity.DeviceAlertRule;
import com.ly.factmesh.iot.domain.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 告警规则引擎
 * 遥测数据上报时根据规则自动触发告警
 * <p>
 * 支持操作符：gt/gte/lt/lte/eq/ne/between/outside
 * 支持测点名称大小写不敏感匹配
 * 规则列表缓存 60 秒，减少 DB 查询
 *
 * @author LY-FactMesh
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertRuleEngineService {

    private static final long RULE_CACHE_TTL_MS = 60_000;

    private final DeviceAlertRuleService deviceAlertRuleService;
    private final DeviceAlertService deviceAlertService;
    private final DeviceRepository deviceRepository;

    private final AtomicReference<CachedRules> rulesCache = new AtomicReference<>();

    private record CachedRules(List<DeviceAlertRule> rules, long timestamp) {
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > RULE_CACHE_TTL_MS;
        }
    }

    /**
     * 使规则缓存失效（规则增删改后调用）
     */
    public void invalidateRuleCache() {
        rulesCache.set(null);
    }

    /**
     * 根据遥测数据评估规则并触发告警
     *
     * @param deviceId   设备ID
     * @param deviceCode 设备编码
     * @param data       遥测数据 key=测点名称, value=数值
     */
    public void evaluate(Long deviceId, String deviceCode, Map<String, Number> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        String deviceType = deviceRepository.findById(deviceId)
                .map(d -> d.getDevice().getDeviceType())
                .orElse(null);

        List<DeviceAlertRule> rules = getCachedOrLoadRules();
        for (DeviceAlertRule rule : rules) {
            if (!matchesDevice(rule, deviceId, deviceType)) {
                continue;
            }

            Number value = getFieldValueCaseInsensitive(data, rule.getFieldName());
            if (value == null) {
                continue;
            }

            double v = value.doubleValue();
            double threshold = rule.getThresholdValue() != null ? rule.getThresholdValue() : 0;
            Double thresholdHigh = rule.getThresholdValueHigh();

            if (!evaluateCondition(rule.getOperator(), v, threshold, thresholdHigh)) {
                continue;
            }

            // 冷却检查
            int cooldown = rule.getCooldownSeconds() != null ? rule.getCooldownSeconds() : 300;
            LocalDateTime since = LocalDateTime.now().minusSeconds(cooldown);
            if (deviceAlertService.hasRecentUnresolvedAlert(rule.getId(), deviceId, since)) {
                log.debug("规则 {} 设备 {} 处于冷却期，跳过告警", rule.getId(), deviceId);
                continue;
            }

            // 创建告警
            String content = buildAlertContent(rule, v, threshold, thresholdHigh);
            try {
                deviceAlertService.createAlertFromRule(
                        deviceId, deviceCode, rule.getId(),
                        rule.getAlertType(), rule.getAlertLevel(), content
                );
                log.info("规则 {} 触发告警: 设备 {} 测点 {} 值 {} {}", rule.getId(), deviceId, rule.getFieldName(), v, rule.getOperator());
            } catch (Exception e) {
                log.warn("创建自动告警失败: {}", e.getMessage());
            }
        }
    }

    private List<DeviceAlertRule> getCachedOrLoadRules() {
        CachedRules cached = rulesCache.get();
        if (cached != null && !cached.isExpired()) {
            return cached.rules();
        }
        List<DeviceAlertRule> rules = deviceAlertRuleService.listEnabledRules();
        rulesCache.set(new CachedRules(rules, System.currentTimeMillis()));
        return rules;
    }

    /**
     * 测点名称大小写不敏感查找
     */
    private Number getFieldValueCaseInsensitive(Map<String, Number> data, String fieldName) {
        if (fieldName == null) return null;
        Number v = data.get(fieldName);
        if (v != null) return v;
        for (Map.Entry<String, Number> e : data.entrySet()) {
            if (fieldName.equalsIgnoreCase(e.getKey())) {
                return e.getValue();
            }
        }
        return null;
    }

    private boolean matchesDevice(DeviceAlertRule rule, Long deviceId, String deviceType) {
        if (rule.getDeviceId() != null && !rule.getDeviceId().equals(deviceId)) {
            return false;
        }
        if (rule.getDeviceType() != null && !rule.getDeviceType().isBlank()
                && (deviceType == null || !rule.getDeviceType().equals(deviceType))) {
            return false;
        }
        return true;
    }

    private boolean evaluateCondition(String operator, double value, double threshold, Double thresholdHigh) {
        if (operator == null) return false;
        return switch (operator.toLowerCase()) {
            case DeviceAlertRule.OP_GT -> value > threshold;
            case DeviceAlertRule.OP_GTE -> value >= threshold;
            case DeviceAlertRule.OP_LT -> value < threshold;
            case DeviceAlertRule.OP_LTE -> value <= threshold;
            case DeviceAlertRule.OP_EQ -> Math.abs(value - threshold) < 1e-9;
            case DeviceAlertRule.OP_NE -> Math.abs(value - threshold) >= 1e-9;
            case DeviceAlertRule.OP_BETWEEN -> {
                if (thresholdHigh == null) yield false;
                double low = Math.min(threshold, thresholdHigh);
                double high = Math.max(threshold, thresholdHigh);
                yield value >= low && value <= high;
            }
            case DeviceAlertRule.OP_OUTSIDE -> {
                if (thresholdHigh == null) yield false;
                double low = Math.min(threshold, thresholdHigh);
                double high = Math.max(threshold, thresholdHigh);
                yield value < low || value > high;
            }
            default -> false;
        };
    }

    private String buildAlertContent(DeviceAlertRule rule, double value, double threshold, Double thresholdHigh) {
        String template = rule.getAlertContentTemplate();
        if (template != null && !template.isBlank()) {
            String threshStr = thresholdHigh != null ? threshold + "~" + thresholdHigh : String.valueOf(threshold);
            return template
                    .replace("{value}", String.valueOf(value))
                    .replace("{threshold}", threshStr);
        }
        if (thresholdHigh != null) {
            return String.format("测点 %s 值 %.2f %s 阈值 %.2f~%.2f", rule.getFieldName(), value, rule.getOperator(), Math.min(threshold, thresholdHigh), Math.max(threshold, thresholdHigh));
        }
        return String.format("测点 %s 值 %.2f %s 阈值 %.2f", rule.getFieldName(), value, rule.getOperator(), threshold);
    }
}
