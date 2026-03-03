package com.ly.factmesh.admin.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 操作日志创建请求（供网关/拦截器调用）
 *
 * @author LY-FactMesh
 */
@Data
@Schema(description = "操作日志创建请求")
public class OperationLogCreateRequest {

    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "模块")
    private String module;
    @Schema(description = "操作")
    private String operation;
    @Schema(description = "HTTP方法")
    private String method;
    @Schema(description = "请求URL")
    private String url;
    @Schema(description = "请求参数")
    private String params;
    @Schema(description = "IP地址")
    private String ip;
    @Schema(description = "状态：0失败 1成功")
    private Integer status;
    @Schema(description = "错误信息")
    private String errorMsg;
    @Schema(description = "耗时(ms)")
    private Long duration;
}
