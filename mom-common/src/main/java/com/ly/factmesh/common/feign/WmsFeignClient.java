package com.ly.factmesh.common.feign;

import com.ly.factmesh.common.feign.dto.RequisitionCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 WMS 的 Feign 客户端（工单发布触发领料）
 *
 * @author LY-FactMesh
 */
@FeignClient(name = "mom-wms", contextId = "wmsFeignClient")
public interface WmsFeignClient {

    /**
     * 创建领料单（由 MES 工单发布时调用）
     *
     * @param request 领料请求
     * @return 领料单 ID
     */
    @PostMapping("/api/requisitions")
    Long createRequisition(@RequestBody RequisitionCreateRequest request);
}
