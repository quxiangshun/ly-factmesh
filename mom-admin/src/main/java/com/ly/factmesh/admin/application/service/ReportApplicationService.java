package com.ly.factmesh.admin.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ly.factmesh.admin.application.dto.ReportDefDTO;
import com.ly.factmesh.admin.infrastructure.database.entity.ReportDefEntity;
import com.ly.factmesh.admin.infrastructure.database.mapper.ReportDefMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 报表应用服务：模板列表、定义 CRUD、执行（聚合 MES/IoT/QMS 数据）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportApplicationService {

    private final ReportDefMapper reportDefMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<Map<String, Object>> TEMPLATES = List.of(
            Map.<String, Object>of("code", "production_summary", "name", "生产日报汇总", "params", List.of("date")),
            Map.<String, Object>of("code", "production_detail", "name", "生产日报明细", "params", List.of("date", "page", "size")),
            Map.<String, Object>of("code", "capacity", "name", "产线产能统计", "params", List.of("date")),
            Map.<String, Object>of("code", "device_stats", "name", "设备统计", "params", List.of()),
            Map.<String, Object>of("code", "inspection_stats", "name", "质检任务统计", "params", List.of()),
            Map.<String, Object>of("code", "inventory_stats", "name", "库存统计", "params", List.of())
    );

    public List<Map<String, Object>> listTemplates() {
        return new ArrayList<>(TEMPLATES);
    }

    public Page<ReportDefDTO> listDefinitions(int page, int size) {
        Page<ReportDefEntity> p = reportDefMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<ReportDefEntity>().orderByDesc(ReportDefEntity::getUpdateTime)
        );
        Page<ReportDefDTO> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(p.getRecords().stream().map(this::toDto).toList());
        return result;
    }

    public ReportDefDTO getDefinitionById(Long id) {
        ReportDefEntity e = reportDefMapper.selectById(id);
        if (e == null) throw new RuntimeException("报表定义不存在");
        return toDto(e);
    }

    @Transactional
    public ReportDefDTO createDefinition(ReportDefDTO request) {
        ReportDefEntity e = new ReportDefEntity();
        e.setName(request.getName());
        e.setReportType(request.getReportType());
        e.setParamsJson(request.getParamsJson());
        reportDefMapper.insert(e);
        return toDto(e);
    }

    @Transactional
    public ReportDefDTO updateDefinition(Long id, ReportDefDTO request) {
        ReportDefEntity e = reportDefMapper.selectById(id);
        if (e == null) throw new RuntimeException("报表定义不存在");
        e.setName(request.getName());
        e.setReportType(request.getReportType());
        e.setParamsJson(request.getParamsJson());
        reportDefMapper.updateById(e);
        return toDto(e);
    }

    @Transactional
    public void deleteDefinition(Long id) {
        reportDefMapper.deleteById(id);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> request) {
        String reportType;
        Map<String, Object> params = (Map<String, Object>) request.getOrDefault("params", Map.of());

        if (request.get("definitionId") != null) {
            Long defId = ((Number) request.get("definitionId")).longValue();
            ReportDefEntity def = reportDefMapper.selectById(defId);
            if (def == null) throw new RuntimeException("报表定义不存在");
            reportType = def.getReportType();
            if (def.getParamsJson() != null && !def.getParamsJson().isBlank()) {
                try {
                    params = objectMapper.readValue(def.getParamsJson(), new TypeReference<Map<String, Object>>() {});
                } catch (Exception ignored) {}
            }
        } else {
            reportType = (String) request.get("reportType");
            if (reportType == null || reportType.isBlank()) throw new IllegalArgumentException("reportType 或 definitionId 必填");
        }

        return switch (reportType) {
            case "production_summary" -> fetchProductionSummary(params);
            case "production_detail" -> fetchProductionDetail(params);
            case "capacity" -> fetchCapacity(params);
            case "device_stats" -> fetchDeviceStats();
            case "inspection_stats" -> fetchInspectionStats();
            case "inventory_stats" -> fetchInventoryStats();
            default -> throw new IllegalArgumentException("不支持的报表类型: " + reportType);
        };
    }

    private Map<String, Object> fetchProductionSummary(Map<String, Object> params) {
        String date = (String) params.get("date");
        String url = "http://mom-mes/api/work-orders/summary" + (date != null ? "?date=" + date : "");
        Object data = restTemplate.getForObject(url, Map.class);
        return Map.of("columns", List.of("date", "completedCount", "completedQuantity", "inProgressCount", "pausedCount"),
                "data", data != null ? List.of(data) : List.of());
    }

    private Map<String, Object> fetchProductionDetail(Map<String, Object> params) {
        String date = (String) params.get("date");
        int page = params.get("page") != null ? ((Number) params.get("page")).intValue() : 1;
        int size = params.get("size") != null ? ((Number) params.get("size")).intValue() : 50;
        String qs = "?page=" + page + "&size=" + size + (date != null ? "&date=" + date : "");
        List<?> data = restTemplate.getForObject("http://mom-mes/api/work-orders/summary/detail" + qs, List.class);
        return Map.of("columns", List.of("orderCode", "productName", "productCode", "planQuantity", "actualQuantity", "endTime"),
                "data", data != null ? data : List.of());
    }

    private Map<String, Object> fetchCapacity(Map<String, Object> params) {
        String date = (String) params.get("date");
        String url = "http://mom-mes/api/lines/capacity-summary" + (date != null ? "?date=" + date : "");
        List<?> data = restTemplate.getForObject(url, List.class);
        if (data == null) {
            data = List.of();
        }
        return Map.of("columns", List.of("lineCode", "lineName", "status", "date", "completedOrderCount", "completedQuantity"),
                "data", data);
    }

    private Map<String, Object> fetchDeviceStats() {
        Object data = restTemplate.getForObject("http://mom-iot/api/devices/stats", Map.class);
        return Map.of("columns", List.of("total", "onlineCount", "faultCount"),
                "data", data != null ? List.of(data) : List.of());
    }

    private Map<String, Object> fetchInspectionStats() {
        Object data = restTemplate.getForObject("http://mom-qms/api/inspection-tasks/stats", Map.class);
        return Map.of("columns", List.of("total", "pendingCount", "inspectingCount", "completedCount"),
                "data", data != null ? List.of(data) : List.of());
    }

    private Map<String, Object> fetchInventoryStats() {
        Object data = restTemplate.getForObject("http://mom-wms/api/inventory/stats", Map.class);
        return Map.of("columns", List.of("totalRecords", "belowSafeStockCount"),
                "data", data != null ? List.of(data) : List.of());
    }

    private ReportDefDTO toDto(ReportDefEntity e) {
        ReportDefDTO d = new ReportDefDTO();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setReportType(e.getReportType());
        d.setParamsJson(e.getParamsJson());
        d.setCreateTime(e.getCreateTime());
        d.setUpdateTime(e.getUpdateTime());
        return d;
    }
}
