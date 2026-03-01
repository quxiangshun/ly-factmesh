package com.ly.factmesh.gateway.controller;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一API文档聚合控制器
 * 实现所有微服务接口的统一聚合、统一访问、统一管理
 */
@RestController
public class AggregatedApiDocsController {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 聚合所有微服务的API文档
     * @return 聚合后的OpenAPI文档
     */
    @GetMapping({"/v3/api-docs/all", "/local-api-docs"})
    public OpenAPI getAllServicesApiDocs() {
        // 创建聚合后的OpenAPI对象
        OpenAPI aggregatedOpenAPI = new OpenAPI();
        
        // 设置统一的文档信息
        Info info = new Info()
                .title("LY-FactMesh 统一API文档")
                .description("所有微服务接口的统一聚合文档")
                .version("1.0-SNAPSHOT")
                .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"));
        aggregatedOpenAPI.info(info);
        
        // 初始化路径集合
        Paths aggregatedPaths = new Paths();
        
        // 定义需要聚合的微服务列表
        Map<String, String> services = new HashMap<>();
        services.put("mom-admin", "http://localhost:8081/v3/api-docs");
        services.put("mom-gateway", "http://localhost:8080/v3/api-docs/gateway");
        
        // 聚合每个微服务的API文档
        for (Map.Entry<String, String> entry : services.entrySet()) {
            String serviceName = entry.getKey();
            String serviceUrl = entry.getValue();
            
            try {
                // 获取单个微服务的API文档
                ResponseEntity<OpenAPI> response = restTemplate.exchange(
                        serviceUrl,
                        HttpMethod.GET,
                        new HttpEntity<>(new HttpHeaders()),
                        OpenAPI.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    OpenAPI serviceOpenAPI = response.getBody();
                    
                    // 将微服务的路径添加到聚合路径中
                    if (serviceOpenAPI.getPaths() != null) {
                        // 为每个路径添加服务前缀，避免路径冲突
                        serviceOpenAPI.getPaths().forEach((path, pathItem) -> {
                            String prefixedPath = path;
                            // 只有非网关服务才添加前缀
                            if (!"mom-gateway".equals(serviceName)) {
                                prefixedPath = "/" + serviceName + path;
                            }
                            aggregatedPaths.addPathItem(prefixedPath, pathItem);
                        });
                    }
                    
                    // 合并组件（如果需要）
                    if (serviceOpenAPI.getComponents() != null) {
                        if (aggregatedOpenAPI.getComponents() == null) {
                            aggregatedOpenAPI.components(serviceOpenAPI.getComponents());
                        } else {
                            // 合并schemas
                            if (serviceOpenAPI.getComponents().getSchemas() != null) {
                                serviceOpenAPI.getComponents().getSchemas().forEach(
                                        (name, schema) -> aggregatedOpenAPI.getComponents().getSchemas().put(
                                                serviceName + "_" + name, schema
                                        )
                                );
                            }
                            // 合并其他组件（如securitySchemes、responses等）
                            if (serviceOpenAPI.getComponents().getSecuritySchemes() != null) {
                                aggregatedOpenAPI.getComponents().securitySchemes(
                                        serviceOpenAPI.getComponents().getSecuritySchemes()
                                );
                            }
                            if (serviceOpenAPI.getComponents().getResponses() != null) {
                                serviceOpenAPI.getComponents().getResponses().forEach(
                                        (respName, respItem) -> aggregatedOpenAPI.getComponents().getResponses().put(
                                                serviceName + "_" + respName, respItem
                                        )
                                );
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // 记录日志，继续处理其他服务
                System.err.println("Failed to fetch API docs for service " + serviceName + ": " + e.getMessage());
            }
        }
        
        // 设置聚合后的路径
        aggregatedOpenAPI.paths(aggregatedPaths);
        
        return aggregatedOpenAPI;
    }
}
