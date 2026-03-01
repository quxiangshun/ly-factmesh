package com.ly.factmesh.admin.infrastructure.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.factmesh.admin.infrastructure.database.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * 用户Mapper接口
 *
 * @author 屈想顺
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 可选的用户实体
     */
    Optional<UserEntity> findByUsername(@Param("username") String username);
}
