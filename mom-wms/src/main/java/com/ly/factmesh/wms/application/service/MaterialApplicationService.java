package com.ly.factmesh.wms.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.wms.application.dto.MaterialCreateRequest;
import com.ly.factmesh.wms.application.dto.MaterialDTO;
import com.ly.factmesh.wms.application.dto.MaterialUpdateRequest;
import com.ly.factmesh.wms.domain.entity.Material;
import com.ly.factmesh.wms.domain.repository.InventoryRepository;
import com.ly.factmesh.wms.domain.repository.MaterialRequisitionRepository;
import com.ly.factmesh.wms.domain.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialApplicationService {

    private final MaterialRepository materialRepository;
    private final InventoryRepository inventoryRepository;
    private final MaterialRequisitionRepository requisitionRepository;

    @Transactional(rollbackFor = Exception.class)
    public MaterialDTO create(MaterialCreateRequest request) {
        if (materialRepository.findByMaterialCode(request.getMaterialCode()).isPresent()) {
            throw new IllegalArgumentException("物料编码已存在: " + request.getMaterialCode());
        }
        Material m = new Material();
        m.setMaterialCode(request.getMaterialCode());
        m.setMaterialName(request.getMaterialName());
        m.setMaterialType(request.getMaterialType());
        m.setSpecification(request.getSpecification());
        m.setUnit(request.getUnit());
        m.setCreateTime(LocalDateTime.now());
        m.setUpdateTime(LocalDateTime.now());
        Material saved = materialRepository.save(m);
        return toDTO(saved);
    }

    public MaterialDTO getById(Long id) {
        return materialRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("物料不存在: " + id));
    }

    @Transactional(rollbackFor = Exception.class)
    public MaterialDTO update(Long id, MaterialUpdateRequest request) {
        Material m = materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("物料不存在: " + id));
        m.setMaterialName(request.getMaterialName());
        m.setMaterialType(request.getMaterialType());
        m.setSpecification(request.getSpecification());
        m.setUnit(request.getUnit());
        m.setUpdateTime(LocalDateTime.now());
        Material saved = materialRepository.save(m);
        return toDTO(saved);
    }

    public Page<MaterialDTO> page(int pageNum, int pageSize, String materialCode, String materialName, String materialType) {
        long total = materialRepository.count(materialCode, materialName, materialType);
        long offset = (long) (pageNum - 1) * pageSize;
        List<Material> list = materialRepository.findAll(offset, pageSize, materialCode, materialName, materialType);
        List<MaterialDTO> records = list.stream().map(this::toDTO).collect(Collectors.toList());
        Page<MaterialDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        Material m = materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("物料不存在: " + id));
        if (!inventoryRepository.findByMaterialId(id).isEmpty()) {
            throw new IllegalArgumentException("物料已存在库存记录，无法删除");
        }
        if (requisitionRepository.existsDetailByMaterialId(id)) {
            throw new IllegalArgumentException("物料已被领料单引用，无法删除");
        }
        materialRepository.deleteById(id);
    }

    private MaterialDTO toDTO(Material m) {
        MaterialDTO dto = new MaterialDTO();
        dto.setId(m.getId());
        dto.setMaterialCode(m.getMaterialCode());
        dto.setMaterialName(m.getMaterialName());
        dto.setMaterialType(m.getMaterialType());
        dto.setSpecification(m.getSpecification());
        dto.setUnit(m.getUnit());
        dto.setCreateTime(m.getCreateTime());
        dto.setUpdateTime(m.getUpdateTime());
        return dto;
    }
}
