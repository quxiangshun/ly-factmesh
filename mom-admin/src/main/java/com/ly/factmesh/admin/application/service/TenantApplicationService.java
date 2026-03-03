package com.ly.factmesh.admin.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ly.factmesh.admin.application.dto.TenantCreateRequest;
import com.ly.factmesh.admin.application.dto.TenantDTO;
import com.ly.factmesh.admin.application.dto.TenantUpdateRequest;
import com.ly.factmesh.common.enums.TenantStatusEnum;
import com.ly.factmesh.admin.domain.entity.Tenant;
import com.ly.factmesh.admin.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户应用服务
 * <p>
 * 多租户模式下，租户为业务数据隔离单位。状态：启用 / 禁用（禁用后该租户下用户不可登录）。
 * </p>
 *
 * @author LY-FactMesh
 */
@Service
@RequiredArgsConstructor
public class TenantApplicationService {

    private final TenantRepository tenantRepository;

    /** 创建租户，编码唯一；未指定状态时默认启用 */
    @Transactional(rollbackFor = Exception.class)
    public TenantDTO create(TenantCreateRequest request) {
        tenantRepository.findByTenantCode(request.getTenantCode())
                .ifPresent(t -> { throw new IllegalArgumentException("租户编码已存在: " + request.getTenantCode()); });
        Tenant t = new Tenant();
        t.setTenantCode(request.getTenantCode());
        t.setTenantName(request.getTenantName());
        t.setContact(request.getContact());
        t.setPhone(request.getPhone());
        t.setStatus(request.getStatus() != null ? request.getStatus() : TenantStatusEnum.ENABLED.getCode());
        t.setConfig(request.getConfig());
        return toDTO(tenantRepository.save(t));
    }

    public TenantDTO getById(Long id) {
        return tenantRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("租户不存在: " + id));
    }

    public Page<TenantDTO> page(int pageNum, int pageSize) {
        long total = tenantRepository.count();
        long offset = (long) (pageNum - 1) * pageSize;
        List<Tenant> list = tenantRepository.findAll(offset, pageSize);
        List<TenantDTO> records = list.stream().map(this::toDTO).collect(Collectors.toList());
        Page<TenantDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    /** 更新租户信息；可将状态改为禁用以禁止该租户访问 */
    @Transactional(rollbackFor = Exception.class)
    public TenantDTO update(Long id, TenantUpdateRequest request) {
        Tenant t = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("租户不存在: " + id));
        if (request.getTenantName() != null) t.setTenantName(request.getTenantName());
        if (request.getContact() != null) t.setContact(request.getContact());
        if (request.getPhone() != null) t.setPhone(request.getPhone());
        if (request.getStatus() != null) t.setStatus(request.getStatus());
        if (request.getConfig() != null) t.setConfig(request.getConfig());
        t.setUpdateTime(LocalDateTime.now());
        return toDTO(tenantRepository.save(t));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        tenantRepository.deleteById(id);
    }

    private TenantDTO toDTO(Tenant t) {
        TenantDTO dto = new TenantDTO();
        dto.setId(t.getId());
        dto.setTenantCode(t.getTenantCode());
        dto.setTenantName(t.getTenantName());
        dto.setContact(t.getContact());
        dto.setPhone(t.getPhone());
        dto.setStatus(t.getStatus());
        dto.setConfig(t.getConfig());
        dto.setCreateTime(t.getCreateTime());
        dto.setUpdateTime(t.getUpdateTime());
        return dto;
    }
}
