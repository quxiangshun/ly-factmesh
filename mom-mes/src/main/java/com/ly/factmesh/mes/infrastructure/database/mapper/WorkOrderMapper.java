package com.ly.factmesh.mes.infrastructure.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.factmesh.mes.infrastructure.database.entity.WorkOrderEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单 Mapper
 *
 * @author LY-FactMesh
 */
@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrderEntity> {
}
