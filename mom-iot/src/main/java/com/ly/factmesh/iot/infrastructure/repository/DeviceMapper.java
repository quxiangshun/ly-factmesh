package com.ly.factmesh.iot.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.iot.domain.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MyBatis Plus设备Mapper接口
 * 用于设备实体的持久化操作
 *
 * @author 屈想顺
 */
@Mapper
public interface DeviceMapper extends BaseMapper<Device> {
    
    /**
     * 根据设备编码获取设备
     * @param deviceCode 设备编码
     * @return 设备实体，不存在则返回null
     */
    Device selectByDeviceCode(@Param("deviceCode") String deviceCode);
    
    /**
     * 根据设备类型获取设备列表
     * @param deviceType 设备类型
     * @return 设备实体列表
     */
    List<Device> selectByDeviceType(@Param("deviceType") String deviceType);
    
    /**
     * 检查设备编码是否存在
     * @param deviceCode 设备编码
     * @return true表示存在，false表示不存在
     */
    boolean existsByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 统计在线设备数
     */
    long countOnline();

    /**
     * 统计故障设备数
     */
    long countFault();
}