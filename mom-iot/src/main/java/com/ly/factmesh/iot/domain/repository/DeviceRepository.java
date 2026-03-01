package com.ly.factmesh.iot.domain.repository;

import com.ly.factmesh.iot.domain.aggregate.DeviceAggregate;

import java.util.List;
import java.util.Optional;

/**
 * 设备仓储接口
 * 定义设备聚合根的持久化操作
 * 遵循DDD的仓储模式，接口位于领域层，实现位于基础设施层
 *
 * @author 屈想顺
 */
public interface DeviceRepository {

    /**
     * 保存设备聚合根
     * @param deviceAggregate 设备聚合根
     * @return 保存后的设备聚合根
     */
    DeviceAggregate save(DeviceAggregate deviceAggregate);

    /**
     * 根据ID获取设备聚合根
     * @param id 设备ID
     * @return 设备聚合根，不存在则返回Optional.empty()
     */
    Optional<DeviceAggregate> findById(Long id);

    /**
     * 根据设备编码获取设备聚合根
     * @param deviceCode 设备编码
     * @return 设备聚合根，不存在则返回Optional.empty()
     */
    Optional<DeviceAggregate> findByDeviceCode(String deviceCode);

    /**
     * 获取所有设备聚合根列表
     * @return 设备聚合根列表
     */
    List<DeviceAggregate> findAll();

    /**
     * 根据设备类型获取设备聚合根列表
     * @param deviceType 设备类型
     * @return 设备聚合根列表
     */
    List<DeviceAggregate> findByDeviceType(String deviceType);

    /**
     * 删除设备聚合根
     * @param deviceAggregate 设备聚合根
     */
    void delete(DeviceAggregate deviceAggregate);

    /**
     * 根据ID删除设备聚合根
     * @param id 设备ID
     */
    void deleteById(Long id);

    /**
     * 检查设备编码是否存在
     * @param deviceCode 设备编码
     * @return true表示存在，false表示不存在
     */
    boolean existsByDeviceCode(String deviceCode);

    /**
     * 获取设备总数
     * @return 设备总数
     */
    long count();

    /**
     * 统计在线设备数（online_status=1）
     */
    long countOnline();

    /**
     * 统计故障设备数（running_status=2）
     */
    long countFault();
}