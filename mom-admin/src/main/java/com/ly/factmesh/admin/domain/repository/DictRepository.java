package com.ly.factmesh.admin.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.domain.entity.Dict;

import java.util.List;
import java.util.Optional;

/**
 * 字典仓库接口
 *
 * @author 屈想顺
 */
public interface DictRepository {
    
    /**
     * 保存字典
     * @param dict 字典实体
     * @return 保存后的字典实体
     */
    Dict save(Dict dict);
    
    /**
     * 根据ID查询字典
     * @param id 字典ID
     * @return 可选的字典实体
     */
    Optional<Dict> findById(Long id);
    
    /**
     * 根据字典类型和字典代码查询字典
     * @param dictType 字典类型
     * @param dictCode 字典代码
     * @return 可选的字典实体
     */
    Optional<Dict> findByDictTypeAndDictCode(String dictType, String dictCode);
    
    /**
     * 根据字典类型查询字典列表
     * @param dictType 字典类型
     * @return 字典列表
     */
    List<Dict> findByDictType(String dictType);
    
    /**
     * 查询所有字典
     * @return 字典列表
     */
    List<Dict> findAll();
    
    /**
     * 分页查询字典
     * @param page 页码
     * @param size 每页大小
     * @param dictType 字典类型（可选）
     * @return 字典分页列表
     */
    Page<Dict> findByPage(Integer page, Integer size, String dictType);
    
    /**
     * 根据ID删除字典
     * @param id 字典ID
     */
    void deleteById(Long id);
    
    /**
     * 统计字典数量
     * @return 字典数量
     */
    long count();
}