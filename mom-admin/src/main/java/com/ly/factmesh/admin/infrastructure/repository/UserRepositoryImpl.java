package com.ly.factmesh.admin.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.domain.entity.User;
import com.ly.factmesh.admin.domain.repository.UserRepository;
import com.ly.factmesh.admin.infrastructure.database.entity.UserEntity;
import com.ly.factmesh.admin.infrastructure.database.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户仓库实现类
 *
 * @author 屈想顺
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    
    private final UserMapper userMapper;
    
    @Override
    public User save(User user) {
        // 将领域实体转换为数据库实体
        UserEntity userEntity = convertToEntity(user);
        
        // 保存到数据库
        if (userEntity.getId() != null) {
            userMapper.updateById(userEntity);
        } else {
            userMapper.insert(userEntity);
        }
        
        // 将保存后的数据库实体转换为领域实体
        return convertToDomain(userEntity);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        // 从数据库查询
        UserEntity userEntity = userMapper.selectById(id);
        
        // 将数据库实体转换为领域实体
        return Optional.ofNullable(userEntity).map(this::convertToDomain);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        // 从数据库查询
        Optional<UserEntity> optionalEntity = userMapper.findByUsername(username);
        
        // 将数据库实体转换为领域实体
        return optionalEntity.map(this::convertToDomain);
    }
    
    @Override
    public List<User> findAll() {
        // 从数据库查询所有
        List<UserEntity> entities = userMapper.selectList(null);
        
        // 将数据库实体列表转换为领域实体列表
        return entities.stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<User> findByPage(Integer page, Integer size) {
        // 创建分页对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserEntity> pageEntity = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        // 执行分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserEntity> userEntityPage = userMapper.selectPage(pageEntity, null);
        
        // 转换为领域实体分页
        Page<User> userPage = new Page<>();
        userPage.setTotal(userEntityPage.getTotal());
        userPage.setCurrent(userEntityPage.getCurrent());
        userPage.setSize(userEntityPage.getSize());
        userPage.setRecords(userEntityPage.getRecords().stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList()));
        
        return userPage;
    }
    
    @Override
    public void deleteById(Long id) {
        // 从数据库删除
        userMapper.deleteById(id);
    }
    
    @Override
    public long count() {
        // 统计数量
        return userMapper.selectCount(null);
    }
    
    /**
     * 将领域实体转换为数据库实体
     * @param user 领域实体
     * @return 数据库实体
     */
    private UserEntity convertToEntity(User user) {
        UserEntity entity = new UserEntity();
        BeanUtils.copyProperties(user, entity);
        return entity;
    }
    
    /**
     * 将数据库实体转换为领域实体
     * @param entity 数据库实体
     * @return 领域实体
     */
    private User convertToDomain(UserEntity entity) {
        User user = new User(entity.getId());
        BeanUtils.copyProperties(entity, user);
        return user;
    }
}