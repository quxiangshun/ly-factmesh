package com.ly.factmesh.iot.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.factmesh.iot.domain.entity.DeviceAlert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备告警 Mapper
 *
 * @author LY-FactMesh
 */
@Mapper
public interface DeviceAlertMapper extends BaseMapper<DeviceAlert> {
}
