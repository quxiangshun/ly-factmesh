package com.ly.factmesh.iot.presentation.controller;

import com.ly.factmesh.iot.application.dto.DeviceAlertCreateRequest;
import com.ly.factmesh.iot.application.dto.DeviceAlertDTO;
import com.ly.factmesh.iot.application.service.DeviceAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备告警 REST 控制器
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/devices/alerts")
@RequiredArgsConstructor
@Tag(name = "设备告警", description = "设备告警创建、查询、处理")
public class DeviceAlertController {

    private final DeviceAlertService deviceAlertService;

    @PostMapping
    @Operation(summary = "创建设备告警")
    public ResponseEntity<DeviceAlertDTO> create(@Valid @RequestBody DeviceAlertCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceAlertService.createAlert(request));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "处理告警")
    public ResponseEntity<DeviceAlertDTO> resolve(
            @PathVariable Long id,
            @RequestParam(required = false) String resolvedBy,
            @RequestParam(required = false) String remark
    ) {
        return ResponseEntity.ok(deviceAlertService.resolveAlert(id, resolvedBy, remark));
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "按设备查询告警列表")
    public ResponseEntity<List<DeviceAlertDTO>> listByDevice(
            @PathVariable Long deviceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(deviceAlertService.listByDevice(deviceId, page, size));
    }

    @GetMapping
    @Operation(summary = "全部告警列表")
    public ResponseEntity<List<DeviceAlertDTO>> listAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(deviceAlertService.listAll(page, size));
    }

    @GetMapping("/pending")
    @Operation(summary = "待处理告警列表")
    public ResponseEntity<List<DeviceAlertDTO>> listPending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(deviceAlertService.listPending(page, size));
    }

    @GetMapping("/pending/count")
    @Operation(summary = "待处理告警数量")
    public ResponseEntity<Map<String, Long>> pendingCount() {
        return ResponseEntity.ok(Map.of("count", deviceAlertService.countPending()));
    }
}
