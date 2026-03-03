package com.ly.factmesh.iot.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.factmesh.iot.domain.entity.DeviceMaintenance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备维保记录 Mapper
 *
 * @author LY-FactMesh
 */
@Mapper
public interface DeviceMaintenanceMapper extends BaseMapper<DeviceMaintenance> {
}
