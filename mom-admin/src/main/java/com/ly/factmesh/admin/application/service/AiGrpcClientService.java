package com.ly.factmesh.admin.application.service;

import com.ly.factmesh.ai.grpc.AiServiceGrpc;
import com.ly.factmesh.ai.grpc.AiServiceProto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI gRPC 客户端服务，通过 gRPC 调用 mom-ai
 *
 * @author LY-FactMesh
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiGrpcClientService {

    private final AiServiceGrpc.AiServiceBlockingStub aiStub;

    /**
     * 获取 AI 能力列表（gRPC 调用）
     */
    public Map<String, Object> getCapabilities() {
        try {
            var req = AiServiceProto.GetCapabilitiesRequest.getDefaultInstance();
            var resp = aiStub.getCapabilities(req);
            List<Map<String, String>> caps = new ArrayList<>();
            for (var c : resp.getCapabilitiesList()) {
                caps.add(Map.of(
                        "code", c.getCode(),
                        "name", c.getName(),
                        "status", c.getStatus(),
                        "description", c.getDescription()
                ));
            }
            return Map.of(
                    "module", resp.getModule(),
                    "version", resp.getVersion(),
                    "lang", resp.getLang(),
                    "capabilities", caps,
                    "source", "grpc"
            );
        } catch (Exception e) {
            log.warn("gRPC 调用 mom-ai GetCapabilities 失败: {}", e.getMessage());
            return Map.of(
                    "module", "mom-ai",
                    "error", e.getMessage(),
                    "source", "grpc",
                    "capabilities", List.<Map<String, String>>of()
            );
        }
    }

    /**
     * 训练模型（gRPC 调用）
     */
    public Map<String, Object> train(int epochs, double lr, List<List<Float>> trainX, List<Integer> trainY) {
        try {
            var req = AiServiceProto.TrainRequest.newBuilder()
                    .setEpochs(epochs)
                    .setLr(lr);
            for (var row : trainX) {
                req.addTrainX(AiServiceProto.FloatArray.newBuilder().addAllValues(row).build());
            }
            req.addAllTrainY(trainY);
            var resp = aiStub.train(req.build());
            return Map.of(
                    "status", resp.getStatus(),
                    "epochs", resp.getEpochs(),
                    "lr", resp.getLr(),
                    "final_loss", resp.getFinalLoss(),
                    "model_path", resp.getModelPath(),
                    "source", "grpc"
            );
        } catch (Exception e) {
            log.warn("gRPC 调用 mom-ai Train 失败: {}", e.getMessage());
            return Map.of("error", e.getMessage(), "source", "grpc");
        }
    }

    /**
     * 推理预测（gRPC 调用）
     */
    public Map<String, Object> predict(List<List<Float>> data) {
        try {
            var req = AiServiceProto.PredictRequest.newBuilder();
            for (var row : data) {
                req.addData(AiServiceProto.FloatArray.newBuilder().addAllValues(row).build());
            }
            var resp = aiStub.predict(req.build());
            List<Integer> preds = new ArrayList<>(resp.getPredictionsList());
            return Map.of(
                    "predictions", preds,
                    "source", "grpc"
            );
        } catch (Exception e) {
            log.warn("gRPC 调用 mom-ai Predict 失败: {}", e.getMessage());
            return Map.of("error", e.getMessage(), "source", "grpc");
        }
    }
}
