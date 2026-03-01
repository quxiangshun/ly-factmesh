package com.ly.factmesh.admin.infrastructure.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.factmesh.admin.infrastructure.database.entity.ConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * 配置Mapper接口
 *
 * @author 屈想顺
 */
@Mapper
public interface ConfigMapper extends BaseMapper<ConfigEntity> {

    /**
     * 根据配置键查询配置
     * @param configKey 配置键
     * @return 可选的配置实体
     */
    Optional<ConfigEntity> findByConfigKey(@Param("configKey") String configKey);
}
