package com.ly.factmesh.admin.infrastructure.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.factmesh.admin.infrastructure.database.entity.TenantEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户 Mapper
 *
 * @author LY-FactMesh
 */
@Mapper
public interface TenantMapper extends BaseMapper<TenantEntity> {
}
