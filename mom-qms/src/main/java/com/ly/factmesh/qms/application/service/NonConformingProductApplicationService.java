package com.ly.factmesh.qms.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.qms.application.dto.NonConformingProductCreateRequest;
import com.ly.factmesh.qms.application.dto.NonConformingProductDTO;
import com.ly.factmesh.qms.application.dto.NcrDisposeRequest;
import com.ly.factmesh.qms.domain.entity.NonConformingProduct;
import com.ly.factmesh.qms.domain.repository.InspectionTaskRepository;
import com.ly.factmesh.qms.domain.repository.NonConformingProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NonConformingProductApplicationService {

    private final NonConformingProductRepository nonConformingProductRepository;
    private final InspectionTaskRepository inspectionTaskRepository;
    private final QualityTraceabilityService qualityTraceabilityService;

    @Transactional(rollbackFor = Exception.class)
    public NonConformingProductDTO create(NonConformingProductCreateRequest request) {
        NonConformingProduct p = new NonConformingProduct();
        p.setNcrNo(generateNcrNo());
        p.setProductCode(request.getProductCode());
        p.setBatchNo(request.getBatchNo());
        p.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
        p.setReason(request.getReason());
        p.setDisposalMethod(request.getDisposalMethod() != null ? request.getDisposalMethod() : NonConformingProduct.DISPOSAL_PENDING);
        p.setDisposalResult(NonConformingProduct.RESULT_PENDING);
        p.setTaskId(request.getTaskId());
        p.setRemark(request.getRemark());
        NonConformingProduct saved = nonConformingProductRepository.save(p);
        String productionOrder = saved.getTaskId() != null
                ? inspectionTaskRepository.findById(saved.getTaskId()).map(t -> t.getOrderCode()).orElse(null)
                : null;
        qualityTraceabilityService.createTraceFromNcr(saved.getId(), saved.getProductCode(), saved.getBatchNo(), productionOrder);
        return toDTO(saved);
    }

    private String generateNcrNo() {
        String prefix = "NCR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String ncrNo = prefix;
        int suffix = 0;
        while (nonConformingProductRepository.findByNcrNo(ncrNo).isPresent()) {
            ncrNo = prefix + "-" + (++suffix);
        }
        return ncrNo;
    }

    public List<NonConformingProductDTO> listByTaskId(Long taskId) {
        return nonConformingProductRepository.findByTaskId(taskId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public NonConformingProductDTO getById(Long id) {
        return nonConformingProductRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("不合格品记录不存在: " + id));
    }

    public Page<NonConformingProductDTO> page(int pageNum, int pageSize, Integer disposalResult, Long taskId) {
        long total = nonConformingProductRepository.countByDisposalResultAndTaskId(disposalResult, taskId);
        long offset = (long) (pageNum - 1) * pageSize;
        List<NonConformingProduct> list = nonConformingProductRepository.findAll(offset, pageSize, disposalResult, taskId);
        List<NonConformingProductDTO> records = list.stream().map(this::toDTO).collect(Collectors.toList());
        Page<NonConformingProductDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    public NonConformingProductDTO dispose(Long id, NcrDisposeRequest request) {
        NonConformingProduct p = nonConformingProductRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("不合格品记录不存在: " + id));
        if (p.getDisposalResult() == NonConformingProduct.RESULT_DONE) {
            throw new IllegalArgumentException("该记录已处置完成");
        }
        if (request != null && request.getDisposalMethod() != null) {
            p.setDisposalMethod(request.getDisposalMethod());
        }
        p.setDisposalResult(NonConformingProduct.RESULT_DONE);
        p.setDisposeTime(LocalDateTime.now());
        if (request != null && request.getRemark() != null) {
            p.setRemark(request.getRemark());
        }
        return toDTO(nonConformingProductRepository.save(p));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        nonConformingProductRepository.deleteById(id);
    }

    private NonConformingProductDTO toDTO(NonConformingProduct p) {
        NonConformingProductDTO dto = new NonConformingProductDTO();
        dto.setId(p.getId());
        dto.setNcrNo(p.getNcrNo());
        dto.setProductCode(p.getProductCode());
        dto.setBatchNo(p.getBatchNo());
        dto.setQuantity(p.getQuantity());
        dto.setReason(p.getReason());
        dto.setDisposalMethod(p.getDisposalMethod());
        dto.setDisposalResult(p.getDisposalResult());
        dto.setTaskId(p.getTaskId());
        dto.setCreateTime(p.getCreateTime());
        dto.setDisposeTime(p.getDisposeTime());
        dto.setRemark(p.getRemark());
        return dto;
    }
}
