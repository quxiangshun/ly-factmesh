package com.ly.factmesh.qms.presentation.controller;

import com.ly.factmesh.qms.application.dto.InspectionResultCreateRequest;
import com.ly.factmesh.qms.application.dto.InspectionResultDTO;
import com.ly.factmesh.qms.application.service.InspectionResultApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 质检结果 REST 控制器
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/inspection-results")
@RequiredArgsConstructor
@Tag(name = "质检结果管理", description = "质检结果录入、查询、删除")
public class InspectionResultController {

    private final InspectionResultApplicationService inspectionResultApplicationService;

    @PostMapping
    @Operation(summary = "创建质检结果")
    public ResponseEntity<InspectionResultDTO> create(@Valid @RequestBody InspectionResultCreateRequest request) {
        InspectionResultDTO dto = inspectionResultApplicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询质检结果")
    public ResponseEntity<InspectionResultDTO> getById(@PathVariable @Parameter(description = "结果ID") Long id) {
        return ResponseEntity.ok(inspectionResultApplicationService.getById(id));
    }

    @GetMapping
    @Operation(summary = "根据任务ID查询质检结果列表")
    public ResponseEntity<List<InspectionResultDTO>> listByTaskId(
            @RequestParam @Parameter(description = "质检任务ID", required = true) Long taskId
    ) {
        return ResponseEntity.ok(inspectionResultApplicationService.listByTaskId(taskId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除质检结果")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inspectionResultApplicationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
