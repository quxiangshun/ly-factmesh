package com.ly.factmesh.admin.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.application.dto.ConfigCreateRequest;
import com.ly.factmesh.admin.application.dto.ConfigDTO;
import com.ly.factmesh.admin.application.dto.ConfigUpdateRequest;
import com.ly.factmesh.admin.application.service.ConfigApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

/**
 * 配置控制器
 *
 * @author 屈想顺
 */
@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
@Tag(name = "配置管理", description = "配置相关的CRUD操作")
public class ConfigController {
    
    private final ConfigApplicationService configApplicationService;
    
    /**
     * 创建配置
     * @param request 创建配置请求
     * @return 创建成功的配置信息
     */
    @PostMapping
    @Operation(summary = "创建配置", description = "创建新的配置信息")
    public ResponseEntity<ConfigDTO> createConfig(@Valid @RequestBody ConfigCreateRequest request) {
        ConfigDTO configDTO = configApplicationService.createConfig(request);
        return new ResponseEntity<>(configDTO, HttpStatus.CREATED);
    }
    
    /**
     * 根据ID查询配置
     * @param id 配置ID
     * @return 配置信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询配置", description = "根据配置ID查询配置信息")
    public ResponseEntity<ConfigDTO> getConfigById(@PathVariable @Parameter(description = "配置ID") Long id) {
        ConfigDTO configDTO = configApplicationService.getConfigById(id);
        return ResponseEntity.ok(configDTO);
    }
    
    /**
     * 根据配置键查询配置
     * @param configKey 配置键
     * @return 配置信息
     */
    @GetMapping("/key/{configKey}")
    @Operation(summary = "根据配置键查询配置", description = "根据配置键查询配置信息")
    public ResponseEntity<ConfigDTO> getConfigByKey(@PathVariable @Parameter(description = "配置键") String configKey) {
        ConfigDTO configDTO = configApplicationService.getConfigByKey(configKey);
        return ResponseEntity.ok(configDTO);
    }
    
    /**
     * 分页查询配置
     * @param page 页码
     * @param size 每页大小
     * @param configKey 配置键（可选）
     * @return 配置分页列表
     */
    @GetMapping
    @Operation(summary = "分页查询配置", description = "分页查询配置列表")
    public ResponseEntity<Page<ConfigDTO>> getConfigs(@RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
                                                    @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size,
                                                    @RequestParam(required = false) @Parameter(description = "配置键（可选）") String configKey) {
        Page<ConfigDTO> configPage = configApplicationService.getConfigs(page, size, configKey);
        return ResponseEntity.ok(configPage);
    }
    
    /**
     * 更新配置
     * @param id 配置ID
     * @param request 更新配置请求
     * @return 更新后的配置信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新配置", description = "根据配置ID更新配置信息")
    public ResponseEntity<ConfigDTO> updateConfig(@PathVariable @Parameter(description = "配置ID") Long id, @Valid @RequestBody ConfigUpdateRequest request) {
        ConfigDTO configDTO = configApplicationService.updateConfig(id, request);
        return ResponseEntity.ok(configDTO);
    }
    
    /**
     * 删除配置
     * @param id 配置ID
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置", description = "根据配置ID删除配置")
    public ResponseEntity<Void> deleteConfig(@PathVariable @Parameter(description = "配置ID") Long id) {
        configApplicationService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 检查配置是否存在
     * @param id 配置ID
     * @return 是否存在
     */
    @GetMapping("/{id}/exists")
    @Operation(summary = "检查配置是否存在", description = "根据配置ID检查配置是否存在")
    public ResponseEntity<Boolean> checkConfigExists(@PathVariable @Parameter(description = "配置ID") Long id) {
        Boolean exists = configApplicationService.checkConfigExists(id);
        return ResponseEntity.ok(exists);
    }
}