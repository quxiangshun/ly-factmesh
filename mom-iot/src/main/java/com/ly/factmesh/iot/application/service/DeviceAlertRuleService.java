package com.ly.factmesh.iot.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.common.utils.SnowflakeIdGenerator;
import com.ly.factmesh.iot.application.dto.DeviceAlertRuleCreateRequest;
import com.ly.factmesh.iot.application.dto.DeviceAlertRuleDTO;
import com.ly.factmesh.iot.application.dto.DeviceAlertRuleUpdateRequest;
import com.ly.factmesh.iot.domain.entity.DeviceAlertRule;
import com.ly.factmesh.iot.infrastructure.repository.DeviceAlertRuleMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备告警规则应用服务
 *
 * @author LY-FactMesh
 */
@Service
public class DeviceAlertRuleService {

    private final DeviceAlertRuleMapper deviceAlertRuleMapper;
    private final AlertRuleEngineService alertRuleEngineService;

    public DeviceAlertRuleService(DeviceAlertRuleMapper deviceAlertRuleMapper,
                                  @Lazy AlertRuleEngineService alertRuleEngineService) {
        this.deviceAlertRuleMapper = deviceAlertRuleMapper;
        this.alertRuleEngineService = alertRuleEngineService;
    }

    public DeviceAlertRuleDTO create(DeviceAlertRuleCreateRequest request) {
        validateThresholdForOperator(request.getOperator(), request.getThresholdValue(), request.getThresholdValueHigh());
        DeviceAlertRule rule = new DeviceAlertRule();
        rule.setId(SnowflakeIdGenerator.getInstance().nextId());
        rule.setRuleName(request.getRuleName());
        rule.setDeviceId(request.getDeviceId());
        rule.setDeviceType(request.getDeviceType());
        rule.setFieldName(request.getFieldName());
        rule.setOperator(request.getOperator());
        rule.setThresholdValue(request.getThresholdValue());
        rule.setThresholdValueHigh(request.getThresholdValueHigh());
        rule.setAlertType(request.getAlertType());
        rule.setAlertLevel(request.getAlertLevel() != null ? request.getAlertLevel() : 2);
        rule.setAlertContentTemplate(request.getAlertContentTemplate());
        rule.setEnabled(request.getEnabled() != null ? request.getEnabled() : 1);
        rule.setCooldownSeconds(request.getCooldownSeconds() != null ? request.getCooldownSeconds() : 300);
        rule.setThresholdValueHigh(request.getThresholdValueHigh());
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        deviceAlertRuleMapper.insert(rule);
        alertRuleEngineService.invalidateRuleCache();
        return toDTO(rule);
    }

    public DeviceAlertRuleDTO update(Long id, DeviceAlertRuleUpdateRequest request) {
        DeviceAlertRule rule = deviceAlertRuleMapper.selectById(id);
        if (rule == null) {
            throw new IllegalArgumentException("规则不存在: " + id);
        }
        if (request.getOperator() != null || request.getThresholdValue() != null || request.getThresholdValueHigh() != null) {
            String op = request.getOperator() != null ? request.getOperator() : rule.getOperator();
            Double th = request.getThresholdValue() != null ? request.getThresholdValue() : rule.getThresholdValue();
            Double thHigh = request.getThresholdValueHigh() != null ? request.getThresholdValueHigh() : rule.getThresholdValueHigh();
            validateThresholdForOperator(op, th, thHigh);
        }
        if (request.getRuleName() != null) rule.setRuleName(request.getRuleName());
        if (request.getDeviceId() != null) rule.setDeviceId(request.getDeviceId());
        if (request.getDeviceType() != null) rule.setDeviceType(request.getDeviceType());
        if (request.getFieldName() != null) rule.setFieldName(request.getFieldName());
        if (request.getOperator() != null) rule.setOperator(request.getOperator());
        if (request.getThresholdValue() != null) rule.setThresholdValue(request.getThresholdValue());
        if (request.getAlertType() != null) rule.setAlertType(request.getAlertType());
        if (request.getAlertLevel() != null) rule.setAlertLevel(request.getAlertLevel());
        if (request.getAlertContentTemplate() != null) rule.setAlertContentTemplate(request.getAlertContentTemplate());
        if (request.getEnabled() != null) rule.setEnabled(request.getEnabled());
        if (request.getCooldownSeconds() != null) rule.setCooldownSeconds(request.getCooldownSeconds());
        if (request.getThresholdValueHigh() != null) rule.setThresholdValueHigh(request.getThresholdValueHigh());
        rule.setUpdateTime(LocalDateTime.now());
        deviceAlertRuleMapper.updateById(rule);
        alertRuleEngineService.invalidateRuleCache();
        return toDTO(rule);
    }

    public void delete(Long id) {
        deviceAlertRuleMapper.deleteById(id);
        alertRuleEngineService.invalidateRuleCache();
    }

    public DeviceAlertRuleDTO getById(Long id) {
        DeviceAlertRule rule = deviceAlertRuleMapper.selectById(id);
        return rule != null ? toDTO(rule) : null;
    }

    public List<DeviceAlertRuleDTO> listAll(int page, int size) {
        Page<DeviceAlertRule> p = new Page<>(page, size);
        LambdaQueryWrapper<DeviceAlertRule> q = new LambdaQueryWrapper<DeviceAlertRule>()
                .orderByDesc(DeviceAlertRule::getCreateTime);
        return deviceAlertRuleMapper.selectPage(p, q).getRecords().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DeviceAlertRuleDTO> listEnabled() {
        return deviceAlertRuleMapper.selectList(
                new LambdaQueryWrapper<DeviceAlertRule>().eq(DeviceAlertRule::getEnabled, 1)
        ).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DeviceAlertRule> listEnabledRules() {
        return deviceAlertRuleMapper.selectList(
                new LambdaQueryWrapper<DeviceAlertRule>().eq(DeviceAlertRule::getEnabled, 1)
        );
    }

    private void validateThresholdForOperator(String operator, Double thresholdValue, Double thresholdValueHigh) {
        if (operator == null) return;
        String op = operator.toLowerCase();
        if ((DeviceAlertRule.OP_BETWEEN.equals(op) || DeviceAlertRule.OP_OUTSIDE.equals(op))
                && thresholdValueHigh == null) {
            throw new IllegalArgumentException("操作符 " + operator + " 需要填写阈值上限 thresholdValueHigh");
        }
    }

    private DeviceAlertRuleDTO toDTO(DeviceAlertRule rule) {
        return DeviceAlertRuleDTO.builder()
                .id(rule.getId())
                .ruleName(rule.getRuleName())
                .deviceId(rule.getDeviceId())
                .deviceType(rule.getDeviceType())
                .fieldName(rule.getFieldName())
                .operator(rule.getOperator())
                .thresholdValue(rule.getThresholdValue())
                .thresholdValueHigh(rule.getThresholdValueHigh())
                .alertType(rule.getAlertType())
                .alertLevel(rule.getAlertLevel())
                .alertContentTemplate(rule.getAlertContentTemplate())
                .enabled(rule.getEnabled())
                .cooldownSeconds(rule.getCooldownSeconds())
                .createTime(rule.getCreateTime())
                .updateTime(rule.getUpdateTime())
                .build();
    }
}
