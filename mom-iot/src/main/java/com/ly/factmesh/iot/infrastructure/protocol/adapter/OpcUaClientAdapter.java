package com.ly.factmesh.iot.infrastructure.protocol.adapter;

import com.ly.factmesh.iot.infrastructure.protocol.api.IndustrialClient;
import com.ly.factmesh.iot.infrastructure.protocol.exception.IndustrialConnException;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * OPC UA 协议适配器，实现统一的 IndustrialClient 接口
 *
 * @author LY-FactMesh
 */
public class OpcUaClientAdapter implements IndustrialClient {

    private OpcUaClient opcUaClient;
    private String endpointUrl;

    @Override
    public boolean connect(String endpointUrl, String username, String password, int connectTimeoutMs) {
        this.endpointUrl = endpointUrl;
        try {
            List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(endpointUrl).get();
            if (endpoints == null || endpoints.isEmpty()) {
                throw new IndustrialConnException("无法获取 OPC UA 端点: " + endpointUrl);
            }
            EndpointDescription endpoint = endpoints.get(0);
            OpcUaClientConfigBuilder configBuilder = new OpcUaClientConfigBuilder();
            configBuilder.setEndpoint(endpoint);
            configBuilder.setRequestTimeout(org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint(connectTimeoutMs));
            // 匿名连接；若需用户名认证，可通过 OpcUaClientConfigBuilder.setIdentityProvider 配置
            opcUaClient = OpcUaClient.create(configBuilder.build());
            opcUaClient.connect().get();
            return true;
        } catch (Exception e) {
            throw new IndustrialConnException("OPC UA 连接失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Object readSingleValue(String pointId) {
        ensureConnected();
        try {
            org.eclipse.milo.opcua.stack.core.types.builtin.NodeId nodeId = org.eclipse.milo.opcua.stack.core.types.builtin.NodeId.parseOrNull(pointId);
            if (nodeId == null) {
                throw new IllegalArgumentException("无效的 OPC UA NodeId: " + pointId);
            }
            org.eclipse.milo.opcua.stack.core.types.builtin.DataValue dataValue =
                    opcUaClient.readValue(0, TimestampsToReturn.Both, nodeId).get();
            return dataValue.getValue().getValue();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new IndustrialConnException("OPC UA 读取单个值失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> readBatchValues(String[] pointIds) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<org.eclipse.milo.opcua.stack.core.types.builtin.NodeId> nodeIds = Arrays.stream(pointIds)
                    .map(org.eclipse.milo.opcua.stack.core.types.builtin.NodeId::parseOrNull)
                    .collect(Collectors.toList());
            List<org.eclipse.milo.opcua.stack.core.types.builtin.DataValue> dataValues =
                    opcUaClient.readValues(0, TimestampsToReturn.Both, nodeIds).get();
            for (int i = 0; i < pointIds.length; i++) {
                result.put(pointIds[i], dataValues.get(i).getValue().getValue());
            }
            return result;
        } catch (Exception e) {
            throw new IndustrialConnException("OPC UA 批量读取失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean writeSingleValue(String pointId, Object value) {
        ensureConnected();
        try {
            org.eclipse.milo.opcua.stack.core.types.builtin.NodeId nodeId =
                    org.eclipse.milo.opcua.stack.core.types.builtin.NodeId.parseOrNull(pointId);
            if (nodeId == null) {
                throw new IllegalArgumentException("无效的 OPC UA NodeId: " + pointId);
            }
            DataValue dataValue = DataValue.valueOnly(new Variant(value));
            WriteValue wv = new WriteValue(nodeId,
                    org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint(13),
                    null, dataValue);
            opcUaClient.write(Collections.singletonList(wv)).get();
            return true;
        } catch (Exception e) {
            throw new IndustrialConnException("OPC UA 写入单个值失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean writeBatchValues(Map<String, Object> pointValueMap) {
        ensureConnected();
        try {
            List<WriteValue> writeValues = pointValueMap.entrySet().stream()
                    .map(e -> {
                        org.eclipse.milo.opcua.stack.core.types.builtin.NodeId nodeId =
                                org.eclipse.milo.opcua.stack.core.types.builtin.NodeId.parseOrNull(e.getKey());
                        DataValue dv = DataValue.valueOnly(new Variant(e.getValue()));
                        return new WriteValue(nodeId,
                                org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint(13),
                                null, dv);
                    })
                    .collect(Collectors.toList());
            opcUaClient.write(writeValues).get();
            return true;
        } catch (Exception e) {
            throw new IndustrialConnException("OPC UA 批量写入失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        if (opcUaClient != null) {
            try {
                opcUaClient.disconnect().get();
            } catch (Exception ignored) {
                // best effort
            }
        }
    }

    @Override
    public boolean isConnected() {
        return opcUaClient != null;
    }

    private void ensureConnected() {
        if (!isConnected()) {
            throw new IndustrialConnException("OPC UA 客户端未连接");
        }
    }
}
