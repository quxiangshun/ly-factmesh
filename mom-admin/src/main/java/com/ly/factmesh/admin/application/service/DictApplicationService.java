package com.ly.factmesh.admin.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.application.dto.DictCreateRequest;
import com.ly.factmesh.admin.application.dto.DictDTO;
import com.ly.factmesh.admin.application.dto.DictUpdateRequest;
import com.ly.factmesh.admin.domain.entity.Dict;
import com.ly.factmesh.admin.domain.repository.DictRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * 字典应用服务
 *
 * @author 屈想顺
 */
@Service
@RequiredArgsConstructor
public class DictApplicationService {
    
    private final DictRepository dictRepository;
    
    /**
     * 创建字典
     * @param request 创建字典请求
     * @return 创建成功的字典DTO
     */
    @Transactional
    public DictDTO createDict(DictCreateRequest request) {
        // 创建字典实体
        Dict dict = new Dict();
        dict.setDictType(request.getDictType());
        dict.setDictCode(request.getDictCode());
        dict.setDictName(request.getDictName());
        dict.setDictValue(request.getDictValue());
        dict.setSortOrder(request.getSortOrder());
        dict.setStatus(request.getStatus());
        
        // 保存字典
        Dict savedDict = dictRepository.save(dict);
        
        // 转换为DTO返回
        return convertToDTO(savedDict);
    }
    
    /**
     * 根据ID查询字典
     * @param id 字典ID
     * @return 字典DTO
     */
    public DictDTO getDictById(Long id) {
        Dict dict = dictRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("字典不存在"));
        
        return convertToDTO(dict);
    }
    
    /**
     * 分页查询字典
     * @param page 页码
     * @param size 每页大小
     * @param dictType 字典类型（可选）
     * @return 字典DTO分页列表
     */
    public Page<DictDTO> getDicts(Integer page, Integer size, String dictType) {
        // 调用仓库层的分页查询
        Page<Dict> dictPage = dictRepository.findByPage(page, size, dictType);
        
        // 转换为DTO分页对象
        Page<DictDTO> dtoPage = new Page<>(page, size);
        dtoPage.setTotal(dictPage.getTotal());
        dtoPage.setRecords(dictPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList()));
        
        return dtoPage;
    }
    
    /**
     * 更新字典
     * @param id 字典ID
     * @param request 更新字典请求
     * @return 更新后的字典DTO
     */
    @Transactional
    public DictDTO updateDict(Long id, DictUpdateRequest request) {
        // 查询字典
        Dict dict = dictRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("字典不存在"));
        
        // 更新字典信息
        if (request.getDictType() != null) {
            dict.setDictType(request.getDictType());
        }
        if (request.getDictCode() != null) {
            dict.setDictCode(request.getDictCode());
        }
        if (request.getDictName() != null) {
            dict.setDictName(request.getDictName());
        }
        if (request.getDictValue() != null) {
            dict.setDictValue(request.getDictValue());
        }
        if (request.getSortOrder() != null) {
            dict.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            dict.setStatus(request.getStatus());
        }
        
        // 保存更新
        Dict updatedDict = dictRepository.save(dict);
        
        return convertToDTO(updatedDict);
    }
    
    /**
     * 删除字典
     * @param id 字典ID
     */
    @Transactional
    public void deleteDict(Long id) {
        // 检查字典是否存在
        dictRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("字典不存在"));
        
        // 删除字典
        dictRepository.deleteById(id);
    }
    
    /**
     * 检查字典是否存在
     * @param id 字典ID
     * @return 是否存在
     */
    public Boolean checkDictExists(Long id) {
        return dictRepository.findById(id).isPresent();
    }

    /**
     * 根据字典类型获取字典列表（按 sort_order 排序）
     */
    public java.util.List<DictDTO> getDictsByType(String dictType) {
        return dictRepository.findByDictType(dictType).stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 将字典实体转换为DTO
     * @param dict 字典实体
     * @return 字典DTO
     */
    private DictDTO convertToDTO(Dict dict) {
        DictDTO dto = new DictDTO();
        BeanUtils.copyProperties(dict, dto);
        return dto;
    }
}