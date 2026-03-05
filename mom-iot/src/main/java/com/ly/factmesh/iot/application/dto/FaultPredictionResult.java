package com.ly.factmesh.iot.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 设备故障预测结果
 *
 * @author LY-FactMesh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备故障预测结果")
public class FaultPredictionResult {

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "风险等级：LOW-低, MEDIUM-中, HIGH-高, CRITICAL-危急")
    private String riskLevel;

    @Schema(description = "风险分数 0-100，越高越危险")
    private int riskScore;

    @Schema(description = "分析时间窗口（小时）")
    private int analysisHours;

    @Schema(description = "风险因素说明")
    private List<String> factors;

    @Schema(description = "建议措施")
    private List<String> recommendations;

    @Schema(description = "数据充分性：是否有足够遥测数据")
    private boolean dataSufficient;
}
