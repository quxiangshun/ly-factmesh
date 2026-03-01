package com.ly.factmesh.admin.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.application.dto.ConfigCreateRequest;
import com.ly.factmesh.admin.application.dto.ConfigDTO;
import com.ly.factmesh.admin.application.dto.ConfigUpdateRequest;
import com.ly.factmesh.admin.domain.entity.Config;
import com.ly.factmesh.admin.domain.repository.ConfigRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * 配置应用服务
 *
 * @author 屈想顺
 */
@Service
@RequiredArgsConstructor
public class ConfigApplicationService {
    
    private final ConfigRepository configRepository;
    
    /**
     * 创建配置
     * @param request 创建配置请求
     * @return 创建成功的配置DTO
     */
    @Transactional
    public ConfigDTO createConfig(ConfigCreateRequest request) {
        // 创建配置实体
        Config config = new Config();
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setConfigDesc(request.getConfigDesc());
        config.setStatus(request.getStatus());
        
        // 保存配置
        Config savedConfig = configRepository.save(config);
        
        // 转换为DTO返回
        return convertToDTO(savedConfig);
    }
    
    /**
     * 根据ID查询配置
     * @param id 配置ID
     * @return 配置DTO
     */
    public ConfigDTO getConfigById(Long id) {
        Config config = configRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在"));
        
        return convertToDTO(config);
    }
    
    /**
     * 根据配置键查询配置
     * @param configKey 配置键
     * @return 配置DTO
     */
    public ConfigDTO getConfigByKey(String configKey) {
        Config config = configRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new RuntimeException("配置不存在"));
        
        return convertToDTO(config);
    }
    
    /**
     * 分页查询配置
     * @param page 页码
     * @param size 每页大小
     * @param configKey 配置键（可选）
     * @return 配置DTO分页列表
     */
    public Page<ConfigDTO> getConfigs(Integer page, Integer size, String configKey) {
        // 调用仓库层的分页查询
        Page<Config> configPage = configRepository.findByPage(page, size, configKey);
        
        // 转换为DTO分页对象
        Page<ConfigDTO> dtoPage = new Page<>(page, size);
        dtoPage.setTotal(configPage.getTotal());
        dtoPage.setRecords(configPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList()));
        
        return dtoPage;
    }
    
    /**
     * 更新配置
     * @param id 配置ID
     * @param request 更新配置请求
     * @return 更新后的配置DTO
     */
    @Transactional
    public ConfigDTO updateConfig(Long id, ConfigUpdateRequest request) {
        // 查询配置
        Config config = configRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在"));
        
        // 更新配置信息
        if (request.getConfigKey() != null) {
            config.setConfigKey(request.getConfigKey());
        }
        if (request.getConfigValue() != null) {
            config.setConfigValue(request.getConfigValue());
        }
        if (request.getConfigDesc() != null) {
            config.setConfigDesc(request.getConfigDesc());
        }
        if (request.getStatus() != null) {
            config.setStatus(request.getStatus());
        }
        
        // 保存更新
        Config updatedConfig = configRepository.save(config);
        
        return convertToDTO(updatedConfig);
    }
    
    /**
     * 删除配置
     * @param id 配置ID
     */
    @Transactional
    public void deleteConfig(Long id) {
        // 检查配置是否存在
        configRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在"));
        
        // 删除配置
        configRepository.deleteById(id);
    }
    
    /**
     * 检查配置是否存在
     * @param id 配置ID
     * @return 是否存在
     */
    public Boolean checkConfigExists(Long id) {
        return configRepository.findById(id).isPresent();
    }
    
    /**
     * 将配置实体转换为DTO
     * @param config 配置实体
     * @return 配置DTO
     */
    private ConfigDTO convertToDTO(Config config) {
        ConfigDTO dto = new ConfigDTO();
        BeanUtils.copyProperties(config, dto);
        return dto;
    }
}