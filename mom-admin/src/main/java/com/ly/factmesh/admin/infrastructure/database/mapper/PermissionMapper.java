package com.ly.factmesh.admin.infrastructure.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.factmesh.admin.infrastructure.database.entity.PermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 权限Mapper接口
 *
 * @author 屈想顺
 */
@Mapper
public interface PermissionMapper extends BaseMapper<PermissionEntity> {

    /**
     * 根据权限代码查询权限
     * @param permCode 权限代码
     * @return 可选的权限实体
     */
    Optional<PermissionEntity> findByPermCode(@Param("permCode") String permCode);

    /**
     * 根据父权限ID查询子权限
     * @param parentId 父权限ID
     * @return 子权限列表
     */
    List<PermissionEntity> findByParentId(@Param("parentId") Long parentId);
}
