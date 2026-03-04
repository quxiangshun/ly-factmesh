package com.ly.factmesh.iot.application.service;

import com.ly.factmesh.iot.application.dto.DeviceBatchImportResult;
import com.ly.factmesh.iot.application.dto.DeviceBatchImportResult.RowError;
import com.ly.factmesh.iot.application.dto.DeviceBatchPreviewResult;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 设备批量导入服务（Excel）
 * Excel 列顺序：设备编码、设备名称、设备类型、型号、制造商、安装位置
 *
 * @author LY-FactMesh
 */
@Service
@RequiredArgsConstructor
public class DeviceBatchImportService {

    private static final String[] HEADERS = {"设备编码", "设备名称", "设备类型", "型号", "制造商", "安装位置"};
    private static final int MAX_ROWS = 2000;

    private final DeviceApplicationService deviceApplicationService;

    /**
     * 预览 Excel：解析并校验，不落库
     */
    public DeviceBatchPreviewResult previewFromExcel(InputStream input) throws IOException {
        List<DeviceBatchPreviewResult.DeviceImportRow> rows = new ArrayList<>();
        List<DeviceBatchPreviewResult.RowError> errors = new ArrayList<>();
        Map<String, Integer> deviceCodeToFirstRow = new HashMap<>();

        try (Workbook wb = WorkbookFactory.create(input)) {
            Sheet sheet = wb.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            formatter.setUseCachedValuesForFormulaCells(true);
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

            Iterator<Row> rowIter = sheet.iterator();
            int excelRowNum = 0;
            while (rowIter.hasNext()) {
                Row row = rowIter.next();
                excelRowNum = row.getRowNum() + 1; // Excel 行号从 1 起
                if (excelRowNum == 1) continue; // 跳过表头
                if (rows.size() >= MAX_ROWS) break;

                String deviceCode = getCellString(row, 0, formatter, evaluator);
                String deviceName = getCellString(row, 1, formatter, evaluator);
                String deviceType = getCellString(row, 2, formatter, evaluator);
                String model = getCellString(row, 3, formatter, evaluator);
                String manufacturer = getCellString(row, 4, formatter, evaluator);
                String installLocation = getCellString(row, 5, formatter, evaluator);

                if (deviceCode == null || deviceCode.isBlank()) {
                    errors.add(new DeviceBatchPreviewResult.RowError(excelRowNum, deviceCode == null ? "" : deviceCode, "设备编码不能为空"));
                    continue;
                }
                if (deviceName == null || deviceName.isBlank()) {
                    errors.add(new DeviceBatchPreviewResult.RowError(excelRowNum, deviceCode, "设备名称不能为空"));
                    continue;
                }
                String code = deviceCode.trim();
                if (deviceCodeToFirstRow.containsKey(code)) {
                    errors.add(new DeviceBatchPreviewResult.RowError(excelRowNum, code, "设备编码与第" + deviceCodeToFirstRow.get(code) + "行重复"));
                    continue;
                }
                deviceCodeToFirstRow.put(code, excelRowNum);

                rows.add(new DeviceBatchPreviewResult.DeviceImportRow(
                        excelRowNum,
                        code,
                        deviceName.trim(),
                        nullIfBlank(deviceType),
                        nullIfBlank(model),
                        nullIfBlank(manufacturer),
                        nullIfBlank(installLocation)
                ));
            }

            if (rows.isEmpty() && errors.isEmpty() && excelRowNum <= 1) {
                return DeviceBatchPreviewResult.builder()
                        .rows(List.of())
                        .errors(List.of(new DeviceBatchPreviewResult.RowError(0, "", "Excel 无数据行，请至少有一行数据（表头除外）")))
                        .build();
            }
        }

        return DeviceBatchPreviewResult.builder()
                .rows(rows)
                .errors(errors)
                .build();
    }

    /**
     * 根据预览数据执行导入
     */
    public DeviceBatchImportResult importFromRows(List<DeviceBatchPreviewResult.DeviceImportRow> rows) {
        List<RowError> errors = new ArrayList<>();
        int successCount = 0;

        for (DeviceBatchPreviewResult.DeviceImportRow r : rows) {
            try {
                deviceApplicationService.registerDevice(
                        r.getDeviceCode(),
                        r.getDeviceName(),
                        r.getDeviceType(),
                        r.getModel(),
                        r.getManufacturer(),
                        r.getInstallLocation()
                );
                successCount++;
            } catch (Exception e) {
                errors.add(new RowError(r.getRow(), r.getDeviceCode(), e.getMessage()));
            }
        }

        return DeviceBatchImportResult.builder()
                .successCount(successCount)
                .failCount(errors.size())
                .errors(errors)
                .build();
    }

    /**
     * 生成导入模板 Excel
     */
    public void writeTemplate(OutputStream output) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("设备导入");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
            }
            // 示例行
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("DEV-001");
            exampleRow.createCell(1).setCellValue("示例设备");
            exampleRow.createCell(2).setCellValue("PLC");
            exampleRow.createCell(3).setCellValue("S7-1200");
            exampleRow.createCell(4).setCellValue("西门子");
            exampleRow.createCell(5).setCellValue("1号车间");

            wb.write(output);
        }
    }

    private static String getCellString(Row row, int col, DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        try {
            String val = formatter.formatCellValue(cell, evaluator);
            return (val != null && !val.isBlank()) ? val.trim() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String nullIfBlank(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }
}
