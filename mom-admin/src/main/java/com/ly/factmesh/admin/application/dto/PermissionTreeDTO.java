package com.ly.factmesh.admin.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限树形 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PermissionTreeDTO extends PermissionDTO {

    private List<PermissionTreeDTO> children = new ArrayList<>();
}
