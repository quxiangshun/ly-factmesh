package com.ly.factmesh.admin.infrastructure.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.domain.entity.Permission;
import com.ly.factmesh.admin.domain.repository.PermissionRepository;
import com.ly.factmesh.admin.infrastructure.database.entity.PermissionEntity;
import com.ly.factmesh.admin.infrastructure.database.mapper.PermissionMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 权限仓库实现类
 *
 * @author 屈想顺
 */
@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {
    
    private final PermissionMapper permissionMapper;
    
    @Override
    public Permission save(Permission permission) {
        // 将领域实体转换为数据库实体
        PermissionEntity permissionEntity = convertToEntity(permission);
        
        // 保存到数据库
        if (permissionEntity.getId() != null) {
            permissionMapper.updateById(permissionEntity);
        } else {
            permissionMapper.insert(permissionEntity);
        }
        
        // 将保存后的数据库实体转换为领域实体
        return convertToDomain(permissionEntity);
    }
    
    @Override
    public Optional<Permission> findById(Long id) {
        // 从数据库查询
        PermissionEntity permissionEntity = permissionMapper.selectById(id);
        
        // 将数据库实体转换为领域实体
        return Optional.ofNullable(permissionEntity).map(this::convertToDomain);
    }
    
    @Override
    public Optional<Permission> findByPermCode(String permCode) {
        // 从数据库查询
        Optional<PermissionEntity> optionalEntity = permissionMapper.findByPermCode(permCode);
        
        // 将数据库实体转换为领域实体
        return optionalEntity.map(this::convertToDomain);
    }
    
    @Override
    public List<Permission> findByParentId(Long parentId) {
        // 从数据库查询
        List<PermissionEntity> entities = permissionMapper.findByParentId(parentId);
        
        // 将数据库实体列表转换为领域实体列表
        return entities.stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Permission> findAll() {
        // 从数据库查询所有
        List<PermissionEntity> entities = permissionMapper.selectList(null);
        
        // 将数据库实体列表转换为领域实体列表
        return entities.stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<Permission> findByPage(Integer page, Integer size) {
        // 创建分页对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<PermissionEntity> pageEntity = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        // 执行分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<PermissionEntity> permissionEntityPage = permissionMapper.selectPage(pageEntity, null);
        
        // 转换为领域实体分页
        Page<Permission> permissionPage = new Page<>();
        permissionPage.setTotal(permissionEntityPage.getTotal());
        permissionPage.setCurrent(permissionEntityPage.getCurrent());
        permissionPage.setSize(permissionEntityPage.getSize());
        permissionPage.setRecords(permissionEntityPage.getRecords().stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList()));
        
        return permissionPage;
    }
    
    @Override
    public void deleteById(Long id) {
        // 从数据库删除
        permissionMapper.deleteById(id);
    }
    
    @Override
    public long count() {
        // 统计数量
        return permissionMapper.selectCount(null);
    }
    
    /**
     * 将领域实体转换为数据库实体
     * @param permission 领域实体
     * @return 数据库实体
     */
    private PermissionEntity convertToEntity(Permission permission) {
        PermissionEntity entity = new PermissionEntity();
        BeanUtils.copyProperties(permission, entity);
        return entity;
    }
    
    /**
     * 将数据库实体转换为领域实体
     * @param entity 数据库实体
     * @return 领域实体
     */
    private Permission convertToDomain(PermissionEntity entity) {
        Permission permission = new Permission();
        BeanUtils.copyProperties(entity, permission);
        return permission;
    }
}