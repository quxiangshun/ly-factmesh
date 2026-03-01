package com.ly.factmesh.admin.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.application.dto.DictCreateRequest;
import com.ly.factmesh.admin.application.dto.DictDTO;
import com.ly.factmesh.admin.application.dto.DictUpdateRequest;
import com.ly.factmesh.admin.application.service.DictApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

/**
 * 字典控制器
 *
 * @author 屈想顺
 */
@RestController
@RequestMapping("/api/dicts")
@RequiredArgsConstructor
@Tag(name = "字典管理", description = "字典相关的CRUD操作")
public class DictController {
    
    private final DictApplicationService dictApplicationService;
    
    /**
     * 创建字典
     * @param request 创建字典请求
     * @return 创建成功的字典信息
     */
    @PostMapping
    @Operation(summary = "创建字典", description = "创建新的字典信息")
    public ResponseEntity<DictDTO> createDict(@Valid @RequestBody DictCreateRequest request) {
        DictDTO dictDTO = dictApplicationService.createDict(request);
        return new ResponseEntity<>(dictDTO, HttpStatus.CREATED);
    }
    
    /**
     * 根据ID查询字典
     * @param id 字典ID
     * @return 字典信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询字典", description = "根据字典ID查询字典信息")
    public ResponseEntity<DictDTO> getDictById(@PathVariable @Parameter(description = "字典ID") Long id) {
        DictDTO dictDTO = dictApplicationService.getDictById(id);
        return ResponseEntity.ok(dictDTO);
    }
    
    /**
     * 分页查询字典
     * @param page 页码
     * @param size 每页大小
     * @param dictType 字典类型（可选）
     * @return 字典分页列表
     */
    @GetMapping
    @Operation(summary = "分页查询字典", description = "分页查询字典列表")
    public ResponseEntity<Page<DictDTO>> getDicts(@RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
                                                  @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size,
                                                  @RequestParam(required = false) @Parameter(description = "字典类型（可选）") String dictType) {
        Page<DictDTO> dictPage = dictApplicationService.getDicts(page, size, dictType);
        return ResponseEntity.ok(dictPage);
    }
    
    /**
     * 更新字典
     * @param id 字典ID
     * @param request 更新字典请求
     * @return 更新后的字典信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新字典", description = "根据字典ID更新字典信息")
    public ResponseEntity<DictDTO> updateDict(@PathVariable @Parameter(description = "字典ID") Long id, @Valid @RequestBody DictUpdateRequest request) {
        DictDTO dictDTO = dictApplicationService.updateDict(id, request);
        return ResponseEntity.ok(dictDTO);
    }
    
    /**
     * 删除字典
     * @param id 字典ID
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除字典", description = "根据字典ID删除字典")
    public ResponseEntity<Void> deleteDict(@PathVariable @Parameter(description = "字典ID") Long id) {
        dictApplicationService.deleteDict(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 检查字典是否存在
     * @param id 字典ID
     * @return 是否存在
     */
    @GetMapping("/{id}/exists")
    @Operation(summary = "检查字典是否存在", description = "根据字典ID检查字典是否存在")
    public ResponseEntity<Boolean> checkDictExists(@PathVariable @Parameter(description = "字典ID") Long id) {
        Boolean exists = dictApplicationService.checkDictExists(id);
        return ResponseEntity.ok(exists);
    }
}