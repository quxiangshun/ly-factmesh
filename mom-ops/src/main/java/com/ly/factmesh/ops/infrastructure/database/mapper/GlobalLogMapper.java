package com.ly.factmesh.ops.infrastructure.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.factmesh.ops.infrastructure.database.entity.GlobalLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GlobalLogMapper extends BaseMapper<GlobalLogEntity> {
}
