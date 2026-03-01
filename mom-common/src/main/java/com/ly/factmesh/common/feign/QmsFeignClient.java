package com.ly.factmesh.common.feign;

import com.ly.factmesh.common.feign.dto.IdResponse;
import com.ly.factmesh.common.feign.dto.QmsInspectionTaskRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 QMS 的 Feign 客户端（报工/工单完成触发质检）
 *
 * @author LY-FactMesh
 */
@FeignClient(name = "mom-qms", contextId = "qmsFeignClient")
public interface QmsFeignClient {

    /**
     * 创建质检任务（由 MES 工单完成/报工时调用）
     *
     * @param request 质检任务请求
     * @return 响应（含 id）
     */
    @PostMapping("/api/inspection-tasks")
    IdResponse createInspectionTask(@RequestBody QmsInspectionTaskRequest request);
}
