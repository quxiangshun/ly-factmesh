package com.ly.factmesh.iot.presentation.controller;

import com.ly.factmesh.iot.application.dto.DeviceDTO;
import com.ly.factmesh.iot.application.dto.DeviceRegisterRequest;
import com.ly.factmesh.iot.application.dto.DeviceStatsDTO;
import com.ly.factmesh.iot.application.dto.DeviceUpdateRequest;
import com.ly.factmesh.iot.application.service.DeviceApplicationService;
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
 * 设备 REST 控制器
 * 提供设备注册、上下线、状态查询等 API
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "设备管理", description = "设备注册、状态、上下线、采集与告警")
public class DeviceController {

    private final DeviceApplicationService deviceApplicationService;

    @PostMapping
    @Operation(summary = "注册设备", description = "注册新设备到平台")
    public ResponseEntity<DeviceDTO> register(@Valid @RequestBody DeviceRegisterRequest request) {
        DeviceDTO dto = deviceApplicationService.registerDevice(
                request.getDeviceCode(),
                request.getDeviceName(),
                request.getDeviceType(),
                request.getModel(),
                request.getManufacturer(),
                request.getInstallLocation()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/stats")
    @Operation(summary = "设备统计", description = "获取设备总数、在线数、故障数")
    public ResponseEntity<DeviceStatsDTO> stats() {
        return ResponseEntity.ok(deviceApplicationService.getDeviceStats());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询设备")
    public ResponseEntity<DeviceDTO> getById(@PathVariable @Parameter(description = "设备ID") Long id) {
        return deviceApplicationService.getDeviceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "设备列表", description = "查询全部设备")
    public ResponseEntity<List<DeviceDTO>> list() {
        return ResponseEntity.ok(deviceApplicationService.getAllDevices());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新设备信息")
    public ResponseEntity<DeviceDTO> update(
            @PathVariable Long id,
            @RequestBody(required = false) DeviceUpdateRequest request
    ) {
        if (request == null) {
            request = new DeviceUpdateRequest();
        }
        return ResponseEntity.ok(deviceApplicationService.updateDevice(id, request));
    }

    @GetMapping("/type/{deviceType}")
    @Operation(summary = "按类型查询设备")
    public ResponseEntity<List<DeviceDTO>> listByType(@PathVariable @Parameter(description = "设备类型") String deviceType) {
        return ResponseEntity.ok(deviceApplicationService.getDevicesByType(deviceType));
    }

    @PostMapping("/{id}/online")
    @Operation(summary = "设备上线")
    public ResponseEntity<DeviceDTO> online(@PathVariable Long id) {
        return ResponseEntity.ok(deviceApplicationService.deviceOnline(id));
    }

    @PostMapping("/{id}/offline")
    @Operation(summary = "设备离线")
    public ResponseEntity<DeviceDTO> offline(@PathVariable Long id) {
        return ResponseEntity.ok(deviceApplicationService.deviceOffline(id));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "设备开始运行")
    public ResponseEntity<DeviceDTO> startRunning(@PathVariable Long id) {
        return ResponseEntity.ok(deviceApplicationService.startRunning(id));
    }

    @PostMapping("/{id}/stop")
    @Operation(summary = "设备停止运行")
    public ResponseEntity<DeviceDTO> stopRunning(@PathVariable Long id) {
        return ResponseEntity.ok(deviceApplicationService.stopRunning(id));
    }

    @PostMapping("/{id}/fault")
    @Operation(summary = "设备故障")
    public ResponseEntity<DeviceDTO> fault(@PathVariable Long id) {
        return ResponseEntity.ok(deviceApplicationService.deviceFault(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新设备状态数据", description = "更新温度、湿度、电压、电流等采集数据")
    public ResponseEntity<DeviceDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam(required = false) Float temperature,
            @RequestParam(required = false) Float humidity,
            @RequestParam(required = false) Float voltage,
            @RequestParam(required = false) Float current
    ) {
        return ResponseEntity.ok(deviceApplicationService.updateDeviceStatus(id, temperature, humidity, voltage, current));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除设备")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deviceApplicationService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}
