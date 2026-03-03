package com.ly.factmesh.mes.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.mes.application.dto.ProcessCreateRequest;
import com.ly.factmesh.mes.application.dto.ProcessDTO;
import com.ly.factmesh.mes.application.dto.ProcessUpdateRequest;
import com.ly.factmesh.mes.domain.entity.Process;
import com.ly.factmesh.mes.domain.repository.ProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工序应用服务
 *
 * @author LY-FactMesh
 */
@Service
@RequiredArgsConstructor
public class ProcessApplicationService {

    private final ProcessRepository processRepository;

    @Transactional(rollbackFor = Exception.class)
    public ProcessDTO create(ProcessCreateRequest request) {
        if (processRepository.findByProcessCode(request.getProcessCode()).isPresent()) {
            throw new IllegalArgumentException("工序编码已存在: " + request.getProcessCode());
        }
        Process p = new Process();
        p.setProcessCode(request.getProcessCode());
        p.setProcessName(request.getProcessName());
        p.setSequence(request.getSequence() != null ? request.getSequence() : 0);
        p.setWorkCenter(request.getWorkCenter());
        p.setCreateTime(LocalDateTime.now());
        Process saved = processRepository.save(p);
        return toDTO(saved);
    }

    public ProcessDTO getById(Long id) {
        return processRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("工序不存在: " + id));
    }

    public Page<ProcessDTO> page(int pageNum, int pageSize) {
        long total = processRepository.count();
        long offset = (long) (pageNum - 1) * pageSize;
        List<Process> list = processRepository.findAll(offset, pageSize);
        List<ProcessDTO> records = list.stream().map(this::toDTO).collect(Collectors.toList());
        Page<ProcessDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    public List<ProcessDTO> listAll() {
        List<Process> list = processRepository.findAll(0, 1000);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public ProcessDTO update(Long id, ProcessUpdateRequest request) {
        Process p = processRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("工序不存在: " + id));
        p.setProcessName(request.getProcessName());
        p.setSequence(request.getSequence() != null ? request.getSequence() : 0);
        p.setWorkCenter(request.getWorkCenter());
        return toDTO(processRepository.save(p));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        processRepository.deleteById(id);
    }

    private ProcessDTO toDTO(Process p) {
        ProcessDTO dto = new ProcessDTO();
        dto.setId(p.getId());
        dto.setProcessCode(p.getProcessCode());
        dto.setProcessName(p.getProcessName());
        dto.setSequence(p.getSequence());
        dto.setWorkCenter(p.getWorkCenter());
        dto.setCreateTime(p.getCreateTime());
        return dto;
    }
}
