package com.ly.factmesh.iot.presentation.controller;

import com.ly.factmesh.iot.application.dto.DeviceMaintenanceCreateRequest;
import com.ly.factmesh.iot.application.dto.DeviceMaintenanceDTO;
import com.ly.factmesh.iot.application.dto.DeviceMaintenanceUpdateRequest;
import com.ly.factmesh.iot.application.service.DeviceMaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备维保记录 REST 控制器
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/devices/maintenance")
@RequiredArgsConstructor
@Tag(name = "设备维保记录", description = "设备台账-维保记录管理")
public class DeviceMaintenanceController {

    private final DeviceMaintenanceService deviceMaintenanceService;

    @PostMapping
    @Operation(summary = "新增维保记录")
    public ResponseEntity<DeviceMaintenanceDTO> create(@Valid @RequestBody DeviceMaintenanceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceMaintenanceService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新维保记录")
    public ResponseEntity<DeviceMaintenanceDTO> update(
            @PathVariable @Parameter(description = "记录ID") Long id,
            @RequestBody DeviceMaintenanceUpdateRequest request
    ) {
        return ResponseEntity.ok(deviceMaintenanceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除维保记录")
    public ResponseEntity<Void> delete(@PathVariable @Parameter(description = "记录ID") Long id) {
        deviceMaintenanceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询维保记录")
    public ResponseEntity<DeviceMaintenanceDTO> getById(@PathVariable @Parameter(description = "记录ID") Long id) {
        DeviceMaintenanceDTO dto = deviceMaintenanceService.getById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "按设备查询维保记录")
    public ResponseEntity<List<DeviceMaintenanceDTO>> listByDevice(
            @PathVariable @Parameter(description = "设备ID") Long deviceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(deviceMaintenanceService.listByDevice(deviceId, page, size));
    }

    @GetMapping
    @Operation(summary = "维保记录列表")
    public ResponseEntity<List<DeviceMaintenanceDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(deviceMaintenanceService.listAll(page, size));
    }
}
