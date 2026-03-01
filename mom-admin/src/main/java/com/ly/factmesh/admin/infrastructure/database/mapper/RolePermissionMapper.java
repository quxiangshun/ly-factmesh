package com.ly.factmesh.admin.infrastructure.database.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关系 Mapper
 */
@Mapper
public interface RolePermissionMapper {

    List<Long> findPermissionIdsByRoleId(@Param("roleId") Long roleId);

    void deleteByRoleId(@Param("roleId") Long roleId);

    void insertBatch(@Param("roleId") Long roleId, @Param("permIds") List<Long> permIds);
}
