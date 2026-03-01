package com.ly.factmesh.admin.infrastructure.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.domain.entity.Role;
import com.ly.factmesh.admin.domain.repository.RoleRepository;
import com.ly.factmesh.admin.infrastructure.database.entity.RoleEntity;
import com.ly.factmesh.admin.infrastructure.database.mapper.RoleMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 角色仓库实现类
 *
 * @author 屈想顺
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {
    
    private final RoleMapper roleMapper;
    
    @Override
    public Role save(Role role) {
        // 将领域实体转换为数据库实体
        RoleEntity roleEntity = convertToEntity(role);
        
        // 保存到数据库
        if (roleEntity.getId() != null) {
            roleMapper.updateById(roleEntity);
        } else {
            roleMapper.insert(roleEntity);
        }
        
        // 将保存后的数据库实体转换为领域实体
        return convertToDomain(roleEntity);
    }
    
    @Override
    public Optional<Role> findById(Long id) {
        // 从数据库查询
        RoleEntity roleEntity = roleMapper.selectById(id);
        
        // 将数据库实体转换为领域实体
        return Optional.ofNullable(roleEntity).map(this::convertToDomain);
    }
    
    @Override
    public Optional<Role> findByRoleName(String roleName) {
        // 从数据库查询
        Optional<RoleEntity> optionalEntity = roleMapper.findByRoleName(roleName);
        
        // 将数据库实体转换为领域实体
        return optionalEntity.map(this::convertToDomain);
    }
    
    @Override
    public Optional<Role> findByRoleCode(String roleCode) {
        // 从数据库查询
        Optional<RoleEntity> optionalEntity = roleMapper.findByRoleCode(roleCode);
        
        // 将数据库实体转换为领域实体
        return optionalEntity.map(this::convertToDomain);
    }
    
    @Override
    public List<Role> findAll() {
        // 从数据库查询所有
        List<RoleEntity> entities = roleMapper.selectList(null);
        
        // 将数据库实体列表转换为领域实体列表
        return entities.stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<Role> findByPage(Integer page, Integer size) {
        // 创建分页对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<RoleEntity> pageEntity = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        // 执行分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<RoleEntity> roleEntityPage = roleMapper.selectPage(pageEntity, null);
        
        // 转换为领域实体分页
        Page<Role> rolePage = new Page<>();
        rolePage.setTotal(roleEntityPage.getTotal());
        rolePage.setCurrent(roleEntityPage.getCurrent());
        rolePage.setSize(roleEntityPage.getSize());
        rolePage.setRecords(roleEntityPage.getRecords().stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList()));
        
        return rolePage;
    }
    
    @Override
    public void deleteById(Long id) {
        // 从数据库删除
        roleMapper.deleteById(id);
    }
    
    @Override
    public long count() {
        // 统计数量
        return roleMapper.selectCount(null);
    }
    
    /**
     * 将领域实体转换为数据库实体
     * @param role 领域实体
     * @return 数据库实体
     */
    private RoleEntity convertToEntity(Role role) {
        RoleEntity entity = new RoleEntity();
        BeanUtils.copyProperties(role, entity);
        return entity;
    }
    
    /**
     * 将数据库实体转换为领域实体
     * @param entity 数据库实体
     * @return 领域实体
     */
    private Role convertToDomain(RoleEntity entity) {
        Role role = new Role();
        BeanUtils.copyProperties(entity, role);
        return role;
    }
}