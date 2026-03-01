package com.ly.factmesh.admin.infrastructure.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.factmesh.admin.infrastructure.database.entity.DictEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 字典Mapper接口
 *
 * @author 屈想顺
 */
@Mapper
public interface DictMapper extends BaseMapper<DictEntity> {

    /**
     * 根据字典类型和字典代码查询字典
     * @param dictType 字典类型
     * @param dictCode 字典代码
     * @return 可选的字典实体
     */
    Optional<DictEntity> findByDictTypeAndDictCode(@Param("dictType") String dictType, @Param("dictCode") String dictCode);

    /**
     * 根据字典类型查询字典列表
     * @param dictType 字典类型
     * @return 字典列表
     */
    List<DictEntity> findByDictType(@Param("dictType") String dictType);
}
