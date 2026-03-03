package com.ly.factmesh.mes.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.mes.application.dto.WorkReportCreateRequest;
import com.ly.factmesh.mes.application.dto.WorkReportDTO;
import com.ly.factmesh.mes.domain.entity.Process;
import com.ly.factmesh.mes.domain.entity.WorkOrder;
import com.ly.factmesh.mes.domain.entity.WorkReport;
import com.ly.factmesh.mes.domain.repository.ProcessRepository;
import com.ly.factmesh.mes.domain.repository.WorkOrderRepository;
import com.ly.factmesh.mes.domain.repository.WorkReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 报工应用服务
 *
 * @author LY-FactMesh
 */
@Service
@RequiredArgsConstructor
public class WorkReportApplicationService {

    private final WorkReportRepository workReportRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ProcessRepository processRepository;

    @Transactional(rollbackFor = Exception.class)
    public WorkReportDTO create(WorkReportCreateRequest request) {
        WorkOrder wo = workOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + request.getOrderId()));
        if (wo.getStatus() != WorkOrder.STATUS_IN_PROGRESS && wo.getStatus() != WorkOrder.STATUS_RELEASED) {
            throw new IllegalStateException("只有已下发或进行中的工单可报工");
        }
        processRepository.findById(request.getProcessId())
                .orElseThrow(() -> new IllegalArgumentException("工序不存在: " + request.getProcessId()));

        WorkReport wr = new WorkReport();
        wr.setOrderId(request.getOrderId());
        wr.setProcessId(request.getProcessId());
        wr.setDeviceId(request.getDeviceId());
        wr.setReportQuantity(request.getReportQuantity());
        wr.setScrapQuantity(request.getScrapQuantity() != null ? request.getScrapQuantity() : 0);
        wr.setReportTime(LocalDateTime.now());
        wr.setOperator(request.getOperator());
        WorkReport saved = workReportRepository.save(wr);

        // 更新工单实际数量（累加）
        int newActual = (wo.getActualQuantity() != null ? wo.getActualQuantity() : 0)
                + request.getReportQuantity()
                - (request.getScrapQuantity() != null ? request.getScrapQuantity() : 0);
        wo.setActualQuantity(Math.max(0, newActual));
        wo.setUpdateTime(LocalDateTime.now());
        WorkOrder updated = workOrderRepository.save(wo);
        if (updated.getStatus() == WorkOrder.STATUS_RELEASED) {
            updated.setStatus(WorkOrder.STATUS_IN_PROGRESS);
            updated.setStartTime(updated.getStartTime() != null ? updated.getStartTime() : LocalDateTime.now());
            workOrderRepository.save(updated);
        }

        return toDTO(saved, wo.getOrderCode(), processRepository.findById(request.getProcessId()).map(Process::getProcessName).orElse(null));
    }

    public WorkReportDTO getById(Long id) {
        WorkReport wr = workReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("报工记录不存在: " + id));
        String orderCode = workOrderRepository.findById(wr.getOrderId()).map(WorkOrder::getOrderCode).orElse(null);
        String processName = processRepository.findById(wr.getProcessId()).map(Process::getProcessName).orElse(null);
        return toDTO(wr, orderCode, processName);
    }

    public Page<WorkReportDTO> page(int pageNum, int pageSize) {
        long total = workReportRepository.count();
        long offset = (long) (pageNum - 1) * pageSize;
        List<WorkReport> list = workReportRepository.findAll(offset, pageSize);
        List<WorkReportDTO> records = list.stream()
                .map(wr -> toDTO(wr,
                        workOrderRepository.findById(wr.getOrderId()).map(WorkOrder::getOrderCode).orElse(null),
                        processRepository.findById(wr.getProcessId()).map(Process::getProcessName).orElse(null)))
                .collect(Collectors.toList());
        Page<WorkReportDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    public Page<WorkReportDTO> pageByOrderId(Long orderId, int pageNum, int pageSize) {
        long total = workReportRepository.countByOrderId(orderId);
        long offset = (long) (pageNum - 1) * pageSize;
        List<WorkReport> list = workReportRepository.findByOrderId(orderId, offset, pageSize);
        String orderCode = workOrderRepository.findById(orderId).map(WorkOrder::getOrderCode).orElse(null);
        List<WorkReportDTO> records = list.stream()
                .map(wr -> toDTO(wr, orderCode,
                        processRepository.findById(wr.getProcessId()).map(Process::getProcessName).orElse(null)))
                .collect(Collectors.toList());
        Page<WorkReportDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        workReportRepository.deleteById(id);
    }

    private WorkReportDTO toDTO(WorkReport wr, String orderCode, String processName) {
        WorkReportDTO dto = new WorkReportDTO();
        dto.setId(wr.getId());
        dto.setOrderId(wr.getOrderId());
        dto.setProcessId(wr.getProcessId());
        dto.setDeviceId(wr.getDeviceId());
        dto.setReportQuantity(wr.getReportQuantity());
        dto.setScrapQuantity(wr.getScrapQuantity());
        dto.setReportTime(wr.getReportTime());
        dto.setOperator(wr.getOperator());
        dto.setOrderCode(orderCode);
        dto.setProcessName(processName);
        return dto;
    }
}
