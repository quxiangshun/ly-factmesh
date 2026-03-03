package com.ly.factmesh.mes.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.mes.application.dto.WorkReportCreateRequest;
import com.ly.factmesh.mes.application.dto.WorkReportDTO;
import com.ly.factmesh.mes.domain.entity.Process;
import com.ly.factmesh.common.enums.WorkOrderStatusEnum;
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
 * <p>
 * 逻辑：报工记录工位/工序的实际产量与报废量，并累加到工单实际数量。
 * 首次报工时若工单为「已下发」状态，则自动推进为「进行中」。
 * </p>
 *
 * @author LY-FactMesh
 */
@Service
@RequiredArgsConstructor
public class WorkReportApplicationService {

    private final WorkReportRepository workReportRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ProcessRepository processRepository;

    /**
     * 创建报工记录。仅已下发或进行中的工单可报工；报工数量累加至工单实际数量
     *
     * @param request 报工参数（工单、工序、产量、报废量等）
     * @return 报工记录 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public WorkReportDTO create(WorkReportCreateRequest request) {
        WorkOrder wo = workOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + request.getOrderId()));
        if (wo.getStatus() == null || (wo.getStatus() != WorkOrderStatusEnum.IN_PROGRESS.getCode() && wo.getStatus() != WorkOrderStatusEnum.RELEASED.getCode())) {
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

        // 业务逻辑：实际数量 = 累计报工合格量 - 累计报废量
        int newActual = (wo.getActualQuantity() != null ? wo.getActualQuantity() : 0)
                + request.getReportQuantity()
                - (request.getScrapQuantity() != null ? request.getScrapQuantity() : 0);
        wo.setActualQuantity(Math.max(0, newActual));
        wo.setUpdateTime(LocalDateTime.now());
        WorkOrder updated = workOrderRepository.save(wo);
        // 首次报工时若工单仍为「已下发」，则自动推进为「进行中」并记录开始时间
        if (updated.getStatus() != null && updated.getStatus() == WorkOrderStatusEnum.RELEASED.getCode()) {
            updated.setStatus(WorkOrderStatusEnum.IN_PROGRESS.getCode());
            updated.setStartTime(updated.getStartTime() != null ? updated.getStartTime() : LocalDateTime.now());
            workOrderRepository.save(updated);
        }

        return toDTO(saved, wo.getOrderCode(), processRepository.findById(request.getProcessId()).map(Process::getProcessName).orElse(null));
    }

    /**
     * 根据 ID 查询报工记录
     *
     * @param id 报工记录 ID
     * @return 报工 DTO
     */
    public WorkReportDTO getById(Long id) {
        WorkReport wr = workReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("报工记录不存在: " + id));
        String orderCode = workOrderRepository.findById(wr.getOrderId()).map(WorkOrder::getOrderCode).orElse(null);
        String processName = processRepository.findById(wr.getProcessId()).map(Process::getProcessName).orElse(null);
        return toDTO(wr, orderCode, processName);
    }

    /** 分页查询报工记录 */
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

    /** 按工单 ID 分页查询报工记录 */
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

    /** 删除报工记录 */
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
