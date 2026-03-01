package com.ly.factmesh.common.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc OpenAPI 配置，用于生成 /v3/api-docs
 * 所有微服务共享此配置
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 配置OpenAPI基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API文档")
                        .description("LY-FactMesh系统的" + applicationName + "微服务API接口文档")
                        .version("1.0-SNAPSHOT")
                        .contact(new Contact()
                                .name("LY-FactMesh团队")
                                .email("ly-factmesh@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("LY-FactMesh文档")
                        .url("https://github.com/ly-factmesh/docs"));
    }

    /**
     * 配置API分组，确保所有控制器都能被扫描到
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .packagesToScan("com.ly.factmesh")
                .build();
    }
}
