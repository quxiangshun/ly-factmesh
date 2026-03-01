package com.ly.factmesh.iot.presentation.controller;

import com.ly.factmesh.iot.application.dto.DeviceTelemetryPoint;
import com.ly.factmesh.iot.application.dto.DeviceTelemetryRequest;
import com.ly.factmesh.iot.application.service.DeviceTelemetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 设备遥测数据 REST 控制器
 * 接收设备/边缘上报的时序数据，支持历史查询
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/devices/telemetry")
@RequiredArgsConstructor
@Tag(name = "设备遥测", description = "设备数据采集上报与历史查询")
public class DeviceTelemetryController {

    private final DeviceTelemetryService deviceTelemetryService;

    @PostMapping("/report")
    @Operation(summary = "上报遥测数据", description = "设备/边缘采集端上报遥测数据到时序库")
    public ResponseEntity<Void> report(@Valid @RequestBody DeviceTelemetryRequest request) {
        deviceTelemetryService.reportTelemetry(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/{deviceId}")
    @Operation(summary = "查询遥测历史", description = "按设备、测点、时间范围查询历史数据")
    public ResponseEntity<List<DeviceTelemetryPoint>> query(
            @PathVariable Long deviceId,
            @RequestParam(required = false) @Parameter(description = "测点名称，如 temperature、voltage") String field,
            @RequestParam(required = false) @Parameter(description = "开始时间 ISO-8601") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @Parameter(description = "结束时间 ISO-8601") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false, defaultValue = "1000") @Parameter(description = "最大条数") Integer limit
    ) {
        Instant startInstant = start != null ? start.atZone(ZoneId.systemDefault()).toInstant() : null;
        Instant endInstant = end != null ? end.atZone(ZoneId.systemDefault()).toInstant() : null;
        List<DeviceTelemetryPoint> points = deviceTelemetryService.queryTelemetry(deviceId, field, startInstant, endInstant, limit);
        return ResponseEntity.ok(points);
    }
}
