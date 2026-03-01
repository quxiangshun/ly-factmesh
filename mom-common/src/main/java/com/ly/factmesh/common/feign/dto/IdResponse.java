package com.ly.factmesh.common.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仅包含 id 的响应（用于 Feign 解析 DTO 中的 id）
 *
 * @author LY-FactMesh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdResponse {
    private Long id;
}
