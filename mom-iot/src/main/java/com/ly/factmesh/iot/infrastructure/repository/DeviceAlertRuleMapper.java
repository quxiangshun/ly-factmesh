package com.ly.factmesh.iot.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.factmesh.iot.domain.entity.DeviceAlertRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备告警规则 Mapper
 *
 * @author LY-FactMesh
 */
@Mapper
public interface DeviceAlertRuleMapper extends BaseMapper<DeviceAlertRule> {
}
