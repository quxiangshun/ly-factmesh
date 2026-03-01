package com.ly.factmesh.admin.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.domain.entity.Dict;
import com.ly.factmesh.admin.domain.repository.DictRepository;
import com.ly.factmesh.admin.infrastructure.database.entity.DictEntity;
import com.ly.factmesh.admin.infrastructure.database.mapper.DictMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 字典仓库实现类
 *
 * @author 屈想顺
 */
@Repository
@RequiredArgsConstructor
public class DictRepositoryImpl implements DictRepository {
    
    private final DictMapper dictMapper;
    
    @Override
    public Dict save(Dict dict) {
        // 将领域实体转换为数据库实体
        DictEntity dictEntity = convertToEntity(dict);
        
        // 保存到数据库
        if (dictEntity.getId() != null) {
            dictMapper.updateById(dictEntity);
        } else {
            dictMapper.insert(dictEntity);
        }
        
        // 将保存后的数据库实体转换为领域实体
        return convertToDomain(dictEntity);
    }
    
    @Override
    public Optional<Dict> findById(Long id) {
        // 从数据库查询
        DictEntity dictEntity = dictMapper.selectById(id);
        
        // 将数据库实体转换为领域实体
        return Optional.ofNullable(dictEntity).map(this::convertToDomain);
    }
    
    @Override
    public Optional<Dict> findByDictTypeAndDictCode(String dictType, String dictCode) {
        // 从数据库查询
        Optional<DictEntity> optionalEntity = dictMapper.findByDictTypeAndDictCode(dictType, dictCode);
        
        // 将数据库实体转换为领域实体
        return optionalEntity.map(this::convertToDomain);
    }
    
    @Override
    public List<Dict> findByDictType(String dictType) {
        // 从数据库查询
        List<DictEntity> entities = dictMapper.findByDictType(dictType);
        
        // 将数据库实体列表转换为领域实体列表
        return entities.stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Dict> findAll() {
        // 从数据库查询所有
        List<DictEntity> entities = dictMapper.selectList(null);
        
        // 将数据库实体列表转换为领域实体列表
        return entities.stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<Dict> findByPage(Integer page, Integer size, String dictType) {
        // 创建分页对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<DictEntity> pageEntity = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        // 构建查询条件
        QueryWrapper<DictEntity> queryWrapper = new QueryWrapper<>();
        if (dictType != null) {
            queryWrapper.eq("dict_type", dictType);
        }
        
        // 执行分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<DictEntity> dictEntityPage = dictMapper.selectPage(pageEntity, queryWrapper);
        
        // 转换为领域实体分页
        Page<Dict> dictPage = new Page<>();
        dictPage.setTotal(dictEntityPage.getTotal());
        dictPage.setCurrent(dictEntityPage.getCurrent());
        dictPage.setSize(dictEntityPage.getSize());
        dictPage.setRecords(dictEntityPage.getRecords().stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList()));
        
        return dictPage;
    }
    
    @Override
    public void deleteById(Long id) {
        // 从数据库删除
        dictMapper.deleteById(id);
    }
    
    @Override
    public long count() {
        // 统计数量
        return dictMapper.selectCount(null);
    }
    
    /**
     * 将领域实体转换为数据库实体
     * @param dict 领域实体
     * @return 数据库实体
     */
    private DictEntity convertToEntity(Dict dict) {
        DictEntity entity = new DictEntity();
        BeanUtils.copyProperties(dict, entity);
        return entity;
    }
    
    /**
     * 将数据库实体转换为领域实体
     * @param entity 数据库实体
     * @return 领域实体
     */
    private Dict convertToDomain(DictEntity entity) {
        Dict dict = new Dict();
        BeanUtils.copyProperties(entity, dict);
        return dict;
    }
}