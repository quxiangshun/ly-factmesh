package com.ly.factmesh.wms.infrastructure.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.factmesh.wms.infrastructure.database.entity.InventoryTransactionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryTransactionMapper extends BaseMapper<InventoryTransactionEntity> {}
