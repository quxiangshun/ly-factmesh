package com.ly.factmesh.iot.presentation.controller;

import com.ly.factmesh.iot.application.dto.DeviceBatchImportResult;
import com.ly.factmesh.iot.application.dto.DeviceBatchPreviewResult;
import com.ly.factmesh.iot.application.dto.DeviceDTO;
import com.ly.factmesh.iot.application.dto.DeviceRegisterRequest;
import com.ly.factmesh.iot.application.dto.DeviceStatsDTO;
import com.ly.factmesh.iot.application.dto.DeviceUpdateRequest;
import com.ly.factmesh.iot.application.service.DeviceApplicationService;
import com.ly.factmesh.iot.application.service.DeviceBatchImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private final DeviceBatchImportService deviceBatchImportService;

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

    @GetMapping("/batch/template")
    @Operation(summary = "下载设备导入模板", description = "下载 Excel 模板，填写后用于批量导入")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        deviceBatchImportService.writeTemplate(out);
        byte[] bytes = out.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "device_import_template.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(bytes.length)
                .body(bytes);
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

    @PostMapping("/batch/preview")
    @Operation(summary = "Excel 导入预览", description = "解析 Excel 并校验，不落库，供用户确认后提交导入")
    public ResponseEntity<DeviceBatchPreviewResult> batchPreview(
            @RequestParam("file") @Parameter(description = "Excel 文件 (.xlsx)") MultipartFile file
    ) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(DeviceBatchPreviewResult.builder()
                    .rows(List.of())
                    .errors(List.of(new DeviceBatchPreviewResult.RowError(0, "", "请选择要上传的 Excel 文件")))
                    .build());
        }
        String name = file.getOriginalFilename();
        if (name == null || (!name.endsWith(".xlsx") && !name.endsWith(".xls"))) {
            return ResponseEntity.badRequest().body(DeviceBatchPreviewResult.builder()
                    .rows(List.of())
                    .errors(List.of(new DeviceBatchPreviewResult.RowError(0, "", "仅支持 .xlsx、.xls 格式")))
                    .build());
        }
        DeviceBatchPreviewResult result = deviceBatchImportService.previewFromExcel(file.getInputStream());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/batch/import")
    @Operation(summary = "确认导入设备", description = "根据预览数据执行导入，需先调用 /batch/preview 获取预览")
    public ResponseEntity<DeviceBatchImportResult> batchImport(
            @RequestBody @Parameter(description = "预览中的有效行数据") List<DeviceBatchPreviewResult.DeviceImportRow> rows
    ) {
        if (rows == null || rows.isEmpty()) {
            return ResponseEntity.badRequest().body(DeviceBatchImportResult.builder()
                    .successCount(0)
                    .failCount(0)
                    .errors(List.of(new DeviceBatchImportResult.RowError(0, "", "没有可导入的数据，请先上传 Excel 并预览")))
                    .build());
        }
        DeviceBatchImportResult result = deviceBatchImportService.importFromRows(rows);
        return ResponseEntity.ok(result);
    }

}
