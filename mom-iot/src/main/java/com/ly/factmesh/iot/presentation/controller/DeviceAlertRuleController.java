package com.ly.factmesh.iot.presentation.controller;

import com.ly.factmesh.iot.application.dto.DeviceAlertRuleCreateRequest;
import com.ly.factmesh.iot.application.dto.DeviceAlertRuleDTO;
import com.ly.factmesh.iot.application.dto.DeviceAlertRuleUpdateRequest;
import com.ly.factmesh.iot.application.service.DeviceAlertRuleService;
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
 * 设备告警规则 REST 控制器
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/devices/alert-rules")
@RequiredArgsConstructor
@Tag(name = "设备告警规则", description = "阈值规则、自动告警")
public class DeviceAlertRuleController {

    private final DeviceAlertRuleService deviceAlertRuleService;

    @PostMapping
    @Operation(summary = "创建告警规则")
    public ResponseEntity<DeviceAlertRuleDTO> create(@Valid @RequestBody DeviceAlertRuleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceAlertRuleService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新告警规则")
    public ResponseEntity<DeviceAlertRuleDTO> update(
            @PathVariable @Parameter(description = "规则ID") Long id,
            @RequestBody DeviceAlertRuleUpdateRequest request
    ) {
        return ResponseEntity.ok(deviceAlertRuleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除告警规则")
    public ResponseEntity<Void> delete(@PathVariable @Parameter(description = "规则ID") Long id) {
        deviceAlertRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询规则")
    public ResponseEntity<DeviceAlertRuleDTO> getById(@PathVariable @Parameter(description = "规则ID") Long id) {
        DeviceAlertRuleDTO dto = deviceAlertRuleService.getById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping
    @Operation(summary = "规则列表")
    public ResponseEntity<List<DeviceAlertRuleDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(deviceAlertRuleService.listAll(page, size));
    }
}
