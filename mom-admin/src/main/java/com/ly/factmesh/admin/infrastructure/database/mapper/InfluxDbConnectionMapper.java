package com.ly.factmesh.admin.infrastructure.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.factmesh.admin.infrastructure.database.entity.InfluxDbConnectionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * InfluxDB 连接配置 Mapper
 *
 * @author 屈想顺
 */
@Mapper
public interface InfluxDbConnectionMapper extends BaseMapper<InfluxDbConnectionEntity> {
}
