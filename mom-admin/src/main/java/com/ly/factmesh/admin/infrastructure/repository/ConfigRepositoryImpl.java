package com.ly.factmesh.admin.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.domain.entity.Config;
import com.ly.factmesh.admin.domain.repository.ConfigRepository;
import com.ly.factmesh.admin.infrastructure.database.entity.ConfigEntity;
import com.ly.factmesh.admin.infrastructure.database.mapper.ConfigMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 配置仓库实现类
 *
 * @author 屈想顺
 */
@Repository
@RequiredArgsConstructor
public class ConfigRepositoryImpl implements ConfigRepository {
    
    private final ConfigMapper configMapper;
    
    @Override
    public Config save(Config config) {
        // 将领域实体转换为数据库实体
        ConfigEntity configEntity = convertToEntity(config);
        
        // 保存到数据库
        if (configEntity.getId() != null) {
            configMapper.updateById(configEntity);
        } else {
            configMapper.insert(configEntity);
        }
        
        // 将保存后的数据库实体转换为领域实体
        return convertToDomain(configEntity);
    }
    
    @Override
    public Optional<Config> findById(Long id) {
        // 从数据库查询
        ConfigEntity configEntity = configMapper.selectById(id);
        
        // 将数据库实体转换为领域实体
        return Optional.ofNullable(configEntity).map(this::convertToDomain);
    }
    
    @Override
    public Optional<Config> findByConfigKey(String configKey) {
        // 从数据库查询
        Optional<ConfigEntity> optionalEntity = configMapper.findByConfigKey(configKey);
        
        // 将数据库实体转换为领域实体
        return optionalEntity.map(this::convertToDomain);
    }
    
    @Override
    public List<Config> findAll() {
        // 从数据库查询所有
        List<ConfigEntity> entities = configMapper.selectList(null);
        
        // 将数据库实体列表转换为领域实体列表
        return entities.stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<Config> findByPage(Integer page, Integer size, String configKey) {
        // 创建分页对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ConfigEntity> pageEntity = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        // 构建查询条件
        QueryWrapper<ConfigEntity> queryWrapper = new QueryWrapper<>();
        if (configKey != null) {
            queryWrapper.like("config_key", configKey);
        }
        
        // 执行分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ConfigEntity> configEntityPage = configMapper.selectPage(pageEntity, queryWrapper);
        
        // 转换为领域实体分页
        Page<Config> configPage = new Page<>();
        configPage.setTotal(configEntityPage.getTotal());
        configPage.setCurrent(configEntityPage.getCurrent());
        configPage.setSize(configEntityPage.getSize());
        configPage.setRecords(configEntityPage.getRecords().stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList()));
        
        return configPage;
    }
    
    @Override
    public void deleteById(Long id) {
        // 从数据库删除
        configMapper.deleteById(id);
    }
    
    @Override
    public long count() {
        // 统计数量
        return configMapper.selectCount(null);
    }
    
    /**
     * 将领域实体转换为数据库实体
     * @param config 领域实体
     * @return 数据库实体
     */
    private ConfigEntity convertToEntity(Config config) {
        ConfigEntity entity = new ConfigEntity();
        BeanUtils.copyProperties(config, entity);
        return entity;
    }
    
    /**
     * 将数据库实体转换为领域实体
     * @param entity 数据库实体
     * @return 领域实体
     */
    private Config convertToDomain(ConfigEntity entity) {
        Config config = new Config();
        BeanUtils.copyProperties(entity, config);
        return config;
    }
}