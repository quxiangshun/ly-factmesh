package com.ly.factmesh.admin.infrastructure.database.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关系 Mapper
 */
@Mapper
public interface UserRoleMapper {

    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

    void deleteByUserId(@Param("userId") Long userId);

    void deleteByRoleId(@Param("roleId") Long roleId);

    void insert(@Param("userId") Long userId, @Param("roleId") Long roleId);

    void insertBatch(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}
