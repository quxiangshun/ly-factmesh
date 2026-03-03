package com.ly.factmesh.mes.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.mes.application.dto.ProductionLineCreateRequest;
import com.ly.factmesh.mes.application.dto.ProductionLineDTO;
import com.ly.factmesh.mes.application.dto.ProductionLineUpdateRequest;
import com.ly.factmesh.mes.domain.entity.ProductionLine;
import com.ly.factmesh.mes.domain.repository.ProductionLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 产线应用服务
 *
 * @author LY-FactMesh
 */
@Service
@RequiredArgsConstructor
public class ProductionLineApplicationService {

    private final ProductionLineRepository productionLineRepository;

    @Transactional(rollbackFor = Exception.class)
    public ProductionLineDTO create(ProductionLineCreateRequest request) {
        if (productionLineRepository.findByLineCode(request.getLineCode()).isPresent()) {
            throw new IllegalArgumentException("产线编码已存在: " + request.getLineCode());
        }
        ProductionLine pl = new ProductionLine();
        pl.setLineCode(request.getLineCode());
        pl.setLineName(request.getLineName());
        pl.setDescription(request.getDescription());
        pl.setSequence(request.getSequence() != null ? request.getSequence() : 0);
        pl.setCreateTime(LocalDateTime.now());
        pl.setUpdateTime(LocalDateTime.now());
        ProductionLine saved = productionLineRepository.save(pl);
        return toDTO(saved);
    }

    public ProductionLineDTO getById(Long id) {
        return productionLineRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("产线不存在: " + id));
    }

    public Page<ProductionLineDTO> page(int pageNum, int pageSize) {
        long total = productionLineRepository.count();
        long offset = (long) (pageNum - 1) * pageSize;
        List<ProductionLine> list = productionLineRepository.findAll(offset, pageSize);
        List<ProductionLineDTO> records = list.stream().map(this::toDTO).collect(Collectors.toList());
        Page<ProductionLineDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    public List<ProductionLineDTO> listAll() {
        List<ProductionLine> list = productionLineRepository.findAll(0, 1000);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public ProductionLineDTO update(Long id, ProductionLineUpdateRequest request) {
        ProductionLine pl = productionLineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("产线不存在: " + id));
        pl.setLineName(request.getLineName());
        pl.setDescription(request.getDescription());
        pl.setSequence(request.getSequence() != null ? request.getSequence() : 0);
        pl.setUpdateTime(LocalDateTime.now());
        return toDTO(productionLineRepository.save(pl));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        productionLineRepository.deleteById(id);
    }

    private ProductionLineDTO toDTO(ProductionLine pl) {
        ProductionLineDTO dto = new ProductionLineDTO();
        dto.setId(pl.getId());
        dto.setLineCode(pl.getLineCode());
        dto.setLineName(pl.getLineName());
        dto.setDescription(pl.getDescription());
        dto.setSequence(pl.getSequence());
        dto.setCreateTime(pl.getCreateTime());
        dto.setUpdateTime(pl.getUpdateTime());
        return dto;
    }
}
