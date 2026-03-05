package com.ly.factmesh.admin.application.config;

import com.ly.factmesh.ai.grpc.AiServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mom-ai gRPC 客户端配置
 *
 * @author LY-FactMesh
 */
@Configuration
public class AiGrpcConfig {

    @Value("${ai.grpc.address:localhost:9098}")
    private String grpcAddress;

    @Bean
    public ManagedChannel aiGrpcChannel() {
        return ManagedChannelBuilder.forTarget(grpcAddress)
                .usePlaintext()
                .build();
    }

    @Bean
    public AiServiceGrpc.AiServiceBlockingStub aiServiceStub(ManagedChannel aiGrpcChannel) {
        return AiServiceGrpc.newBlockingStub(aiGrpcChannel);
    }
}
