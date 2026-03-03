package com.ly.factmesh.mes.infrastructure.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.factmesh.mes.infrastructure.database.entity.WorkReportEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报工记录 Mapper
 *
 * @author LY-FactMesh
 */
@Mapper
public interface WorkReportMapper extends BaseMapper<WorkReportEntity> {
}
