package com.ly.factmesh.mes.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.mes.application.dto.ProductionLineCapacityDTO;
import com.ly.factmesh.mes.application.dto.ProductionLineCreateRequest;
import com.ly.factmesh.mes.application.dto.ProductionLineDTO;
import com.ly.factmesh.mes.application.dto.ProductionLineUpdateRequest;
import com.ly.factmesh.mes.domain.entity.ProductionLine;
import com.ly.factmesh.mes.domain.repository.ProductionLineRepository;
import com.ly.factmesh.mes.domain.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final WorkOrderRepository workOrderRepository;

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
        pl.setStatus(ProductionLine.STATUS_IDLE);
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

    @Transactional(rollbackFor = Exception.class)
    public ProductionLineDTO updateStatus(Long id, Integer status) {
        ProductionLine pl = productionLineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("产线不存在: " + id));
        if (status != null && status >= ProductionLine.STATUS_IDLE && status <= ProductionLine.STATUS_MAINTENANCE) {
            pl.setStatus(status);
            pl.setUpdateTime(LocalDateTime.now());
            return toDTO(productionLineRepository.save(pl));
        }
        throw new IllegalArgumentException("产线状态无效，需为 0-空闲 1-运行 2-检修");
    }

    /**
     * 产线产能统计（按产线汇总完成工单数、产量）
     */
    public List<ProductionLineCapacityDTO> getCapacitySummary(LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        List<ProductionLine> lines = productionLineRepository.findAll(0, 1000);
        List<ProductionLineCapacityDTO> result = new ArrayList<>();
        for (ProductionLine pl : lines) {
            long orderCount = workOrderRepository.countCompletedByLineIdOnDate(pl.getId(), target);
            int quantity = workOrderRepository.sumActualQuantityByLineIdCompletedOnDate(pl.getId(), target);
            result.add(ProductionLineCapacityDTO.builder()
                    .lineId(pl.getId())
                    .lineCode(pl.getLineCode())
                    .lineName(pl.getLineName())
                    .status(pl.getStatus())
                    .date(target)
                    .completedOrderCount(orderCount)
                    .completedQuantity(quantity)
                    .build());
        }
        return result;
    }

    private ProductionLineDTO toDTO(ProductionLine pl) {
        ProductionLineDTO dto = new ProductionLineDTO();
        dto.setId(pl.getId());
        dto.setLineCode(pl.getLineCode());
        dto.setLineName(pl.getLineName());
        dto.setDescription(pl.getDescription());
        dto.setSequence(pl.getSequence());
        dto.setStatus(pl.getStatus());
        dto.setCreateTime(pl.getCreateTime());
        dto.setUpdateTime(pl.getUpdateTime());
        return dto;
    }
}
