package com.ly.factmesh.admin.infrastructure.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.factmesh.admin.infrastructure.database.entity.RoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * 角色Mapper接口
 *
 * @author 屈想顺
 */
@Mapper
public interface RoleMapper extends BaseMapper<RoleEntity> {

    /**
     * 根据角色名称查询角色
     * @param roleName 角色名称
     * @return 可选的角色实体
     */
    Optional<RoleEntity> findByRoleName(@Param("roleName") String roleName);

    /**
     * 根据角色代码查询角色
     * @param roleCode 角色代码
     * @return 可选的角色实体
     */
    Optional<RoleEntity> findByRoleCode(@Param("roleCode") String roleCode);
}
