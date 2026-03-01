package com.ly.factmesh.admin.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.domain.entity.Config;

import java.util.List;
import java.util.Optional;

/**
 * 配置仓库接口
 *
 * @author 屈想顺
 */
public interface ConfigRepository {
    
    /**
     * 保存配置
     * @param config 配置实体
     * @return 保存后的配置实体
     */
    Config save(Config config);
    
    /**
     * 根据ID查询配置
     * @param id 配置ID
     * @return 可选的配置实体
     */
    Optional<Config> findById(Long id);
    
    /**
     * 根据配置键查询配置
     * @param configKey 配置键
     * @return 可选的配置实体
     */
    Optional<Config> findByConfigKey(String configKey);
    
    /**
     * 查询所有配置
     * @return 配置列表
     */
    List<Config> findAll();
    
    /**
     * 分页查询配置
     * @param page 页码
     * @param size 每页大小
     * @param configKey 配置键（可选）
     * @return 配置分页列表
     */
    Page<Config> findByPage(Integer page, Integer size, String configKey);
    
    /**
     * 根据ID删除配置
     * @param id 配置ID
     */
    void deleteById(Long id);
    
    /**
     * 统计配置数量
     * @return 配置数量
     */
    long count();
}