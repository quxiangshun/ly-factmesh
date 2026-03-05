package com.ly.factmesh.admin.presentation.controller;

import com.ly.factmesh.admin.application.service.AiGrpcClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI gRPC 代理接口
 * mom-admin 通过 gRPC 调用 mom-ai，对外暴露 REST 便于前端/联调
 *
 * @author LY-FactMesh
 */
@RestController
@RequestMapping("/api/ai-grpc")
@RequiredArgsConstructor
@Tag(name = "AI gRPC 代理", description = "通过 gRPC 调用 mom-ai，供联调与内部使用")
public class AiGrpcController {

    private final AiGrpcClientService aiGrpcClient;

    @GetMapping("/capabilities")
    @Operation(summary = "获取 AI 能力（gRPC）")
    public ResponseEntity<Map<String, Object>> capabilities() {
        return ResponseEntity.ok(aiGrpcClient.getCapabilities());
    }

    @PostMapping("/train")
    @Operation(summary = "训练模型（gRPC）")
    public ResponseEntity<Map<String, Object>> train(@RequestBody Map<String, Object> body) {
        int epochs = ((Number) body.getOrDefault("epochs", 10)).intValue();
        double lr = ((Number) body.getOrDefault("lr", 0.001)).doubleValue();
        @SuppressWarnings("unchecked")
        List<List<Double>> tx = (List<List<Double>>) body.getOrDefault("train_x", List.of(List.of(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0), List.of(2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0)));
        @SuppressWarnings("unchecked")
        List<Integer> ty = (List<Integer>) body.getOrDefault("train_y", List.of(0, 1));
        List<List<Float>> trainX = tx.stream().map(row -> row.stream().map(Double::floatValue).toList()).toList();
        return ResponseEntity.ok(aiGrpcClient.train(epochs, lr, trainX, ty));
    }

    @PostMapping("/predict")
    @Operation(summary = "推理预测（gRPC）")
    public ResponseEntity<Map<String, Object>> predict(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<List<Double>> data = (List<List<Double>>) body.getOrDefault("data", List.of(List.of(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0)));
        List<List<Float>> dataF = data.stream().map(row -> row.stream().map(Double::floatValue).toList()).toList();
        return ResponseEntity.ok(aiGrpcClient.predict(dataF));
    }
}
