# Knife4j 集成文档

## 1. 概述

Knife4j 是一个基于 Spring Boot 的 API 文档生成工具，它封装了 Swagger/SpringDoc 功能，并提供了更友好的 UI 界面和更多增强功能。本项目采用 Knife4j 实现了所有微服务接口的统一聚合、统一访问、统一管理。

## 2. 技术栈

- Spring Boot 3.x
- Spring Cloud Gateway
- SpringDoc OpenAPI 2.6.0
- Knife4j 4.4.0

## 3. 架构设计

### 3.1 整体架构

```
┌───────────────────────────────────────────────────────────┐
│                   API 网关层                              │
│  ┌─────────────┐  ┌───────────────────────────────────┐  │
│  │ Spring Cloud│  │  Knife4j 网关聚合                 │  │
│  │   Gateway   │  │  ┌─────────────────────────────┐ │  │
│  └─────────────┘  │  │  SwaggerResourceController  │ │  │
│                   │  └─────────────────────────────┘ │  │
│                   └───────────────────────────────────┘  │
└─────────────────────────────┬─────────────────────────────┘
                              │
┌─────────────────────────────▼─────────────────────────────┐
│                   服务层                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │  mom-admin  │  │  mom-iot    │  │  其他服务    │        │
│  ├─────────────┤  ├─────────────┤  ├─────────────┤        │
│  │  Knife4j    │  │  Knife4j    │  │  Knife4j    │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└───────────────────────────────────────────────────────────┘
```

### 3.2 核心组件

1. **mom-common**：提供共享的 Knife4j 配置
2. **mom-gateway**：实现网关聚合功能
3. **各业务服务**：集成 Knife4j，生成各自的 API 文档

## 4. 集成步骤

### 4.1 1. 配置 mom-common 模块

#### 4.1.1 添加依赖

**文件位置**：`mom-common/build.gradle`

**配置内容**：

```gradle
// Knife4j依赖，用于生成OpenAPI文档
api 'com.github.xiaoymin:knife4j-openapi3-jakarta-spring-boot-starter:4.4.0'
```

#### 4.1.2 创建配置类

**文件位置**：`mom-common/src/main/java/com/ly/factmesh/common/config/Knife4jConfig.java`

**创建命令**：

```bash
mkdir -p mom-common/src/main/java/com/ly/factmesh/common/config
```

**配置内容**：

```java
package com.ly.factmesh.common.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;

/**
 * Knife4j配置类，用于生成OpenAPI文档
 * 所有微服务共享此配置
 */
@Configuration
public class Knife4jConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 配置OpenAPI基本信息
     * @return OpenAPI对象
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
     * @return GroupedOpenApi对象
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
```

### 4.2 配置 mom-gateway 模块

#### 4.2.1 添加依赖

**文件位置**：`mom-gateway/build.gradle`

**配置内容**：

```gradle
// Springdoc OpenAPI Gateway依赖
implementation 'org.springdoc:springdoc-openapi-starter-webflux-ui:2.6.0'
implementation 'org.springdoc:springdoc-openapi-starter-webflux-api:2.6.0'

// Knife4j网关聚合依赖
implementation 'com.github.xiaoymin:knife4j-gateway-spring-boot-starter:4.4.0'
```

#### 4.2.2 配置 application.yml

**文件位置**：`mom-gateway/src/main/resources/application.yml`

**配置内容**：

```yaml
# Springdoc OpenAPI配置
springdoc:
  api-docs:
    enabled: true   # 启用网关自己生成OpenAPI文档
    path: /v3/api-docs/gateway  # 网关自身的OpenAPI文档路径
  swagger-ui:
    enabled: true   # 启用Swagger UI
    path: /swagger-ui.html  # Swagger UI路径

# Knife4j网关聚合配置
# 使用manual策略，手工维护服务清单
knife4j:
  enable: true    # 启用Knife4j增强功能
  gateway:
    enabled: true  # 启用网关聚合功能
    strategy: manual  # 手工维护服务清单
    routes:
      # 管理服务
      - name: 管理服务 (mom-admin)
        service-name: mom-admin
        url: /admin/v3/api-docs  # 通过网关访问admin服务的API文档
      # 网关自身
      - name: 网关服务 (mom-gateway)
        service-name: mom-gateway
        url: /v3/api-docs/gateway  # 网关自身的API文档
```

#### 4.2.3 创建 SwaggerResourceController

**文件位置**：`mom-gateway/src/main/java/com/ly/factmesh/gateway/controller/SwaggerResourceController.java`

**创建命令**：

```bash
mkdir -p mom-gateway/src/main/java/com/ly/factmesh/gateway/controller
```

**配置内容**：

```java
package com.ly.factmesh.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger资源控制器，用于提供聚合的Swagger资源列表
 * 确保Knife4j UI能正确识别并显示所有微服务选项
 */
@RestController
public class SwaggerResourceController {

    /**
     * Swagger资源实体类
     * Knife4j UI期望的格式：必须包含name、location、swaggerVersion字段
     */
    static class SwaggerResource {
        private String name;
        private String location;
        private String swaggerVersion;

        public SwaggerResource(String name, String location, String swaggerVersion) {
            this.name = name;
            this.location = location;
            this.swaggerVersion = swaggerVersion;
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getSwaggerVersion() {
            return swaggerVersion;
        }

        public void setSwaggerVersion(String swaggerVersion) {
            this.swaggerVersion = swaggerVersion;
        }
    }

    /**
     * 获取Swagger资源列表 - 这是Knife4j UI从顶部下拉菜单获取微服务列表的关键端点
     * 必须返回正确格式的资源列表，否则Knife4j UI只会显示"default"选项
     * @return Swagger资源列表
     */
    @GetMapping("/swagger-resources")
    public List<SwaggerResource> getSwaggerResources() {
        List<SwaggerResource> resources = new ArrayList<>();
        
        // 添加admin微服务的Swagger资源 - 通过Gateway路由访问
        resources.add(new SwaggerResource("管理服务 (mom-admin)", "/admin/v3/api-docs", "3.0"));
        
        // 添加gateway自身的Swagger资源
        resources.add(new SwaggerResource("网关服务 (mom-gateway)", "/v3/api-docs/gateway", "3.0"));
        
        return resources;
    }
    

    /**
     * 兼容旧版Swagger UI的资源列表端点
     */
    @GetMapping("/swagger-resources/configuration/ui")
    public Object getUiConfiguration() {
        return new Object();
    }

    /**
     * 兼容旧版Swagger UI的安全配置端点
     */
    @GetMapping("/swagger-resources/configuration/security")
    public Object getSecurityConfiguration() {
        return new ArrayList<>();
    }
}
```

### 4.3 业务服务配置

所有业务服务（如 mom-admin、mom-iot 等）只需引入 mom-common 依赖，无需额外配置，即可自动集成 Knife4j。

## 5. 启动服务

### 5.1 启动顺序

1. 先启动所有业务服务（如 mom-admin）
2. 最后启动网关服务（mom-gateway）

### 5.2 启动命令

**启动 admin 服务**：

```bash
# 在项目根目录执行
./gradlew.bat :mom-admin:bootRun
```

**启动 gateway 服务**：

```bash
# 在项目根目录执行
./gradlew.bat :mom-gateway:bootRun
```

## 6. 访问地址

| 名称 | 地址 | 说明 |
|------|------|------|
| Knife4j UI | http://localhost:8080/doc.html | 主文档入口，提供更友好的UI |
| Swagger UI | http://localhost:8080/swagger-ui.html | 标准Swagger UI |
| 网关API文档 | http://localhost:8080/v3/api-docs/gateway | 网关自身的API文档 |
| 管理服务API文档 | http://localhost:8080/admin/v3/api-docs | 通过网关访问admin服务的API文档 |
| Swagger资源列表 | http://localhost:8080/swagger-resources | 用于UI获取微服务列表 |

## 7. 使用说明

### 7.1 访问文档

1. 打开浏览器，访问 `http://localhost:8080/doc.html`
2. 在页面顶部的下拉菜单中选择要查看的微服务
3. 浏览该微服务的所有API接口
4. 可以查看接口详情、参数说明、返回值结构等
5. 支持在线调试接口

### 7.2 在线调试

1. 选择一个API接口
2. 点击「调试」按钮
3. 填写请求参数
4. 点击「发送」按钮
5. 查看响应结果

## 8. 验证配置

### 8.1 验证服务状态

**检查 admin 服务是否启动**：

```bash
# 在浏览器访问或使用curl命令
curl http://localhost:8081/v3/api-docs
```

**检查 gateway 服务是否启动**：

```bash
# 在浏览器访问或使用curl命令
curl http://localhost:8080/v3/api-docs/gateway
```

**检查 Swagger 资源列表**：

```bash
# 在浏览器访问或使用curl命令
curl http://localhost:8080/swagger-resources
```

## 9. 配置详解

### 9.1 Knife4j 网关聚合配置

| 配置项 | 说明 | 可选值 |
|--------|------|--------|
| knife4j.gateway.enabled | 是否启用网关聚合 | true/false |
| knife4j.gateway.strategy | 服务发现策略 | manual/discover |
| knife4j.gateway.routes | 服务路由配置 | - |
| knife4j.gateway.routes[].name | 服务显示名称 | - |
| knife4j.gateway.routes[].service-name | 服务名称 | - |
| knife4j.gateway.routes[].url | 服务API文档地址 | - |

### 9.2 SpringDoc 配置

| 配置项 | 说明 |
|--------|------|
| springdoc.api-docs.enabled | 是否启用API文档生成 |
| springdoc.api-docs.path | API文档路径 |
| springdoc.swagger-ui.enabled | 是否启用Swagger UI |
| springdoc.swagger-ui.path | Swagger UI路径 |
| springdoc.swagger-ui.urls | Swagger UI中显示的服务列表 |

## 10. 扩展说明

### 10.1 添加新服务

当需要添加新服务到文档聚合中时，只需：

1. 在新服务中引入 mom-common 依赖
2. 在 `mom-gateway` 的 `application.yml` 中添加该服务的路由配置
3. 在 `SwaggerResourceController` 中添加该服务的资源

### 10.2 自定义文档内容

可以通过在控制器和方法上添加 Swagger 注解来自定义文档内容：

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户", description = "根据用户ID获取详细信息")
    @Parameter(name = "id", description = "用户ID", required = true)
    public UserDTO getUserById(@PathVariable Long id) {
        // 实现逻辑
    }
    
    // 其他方法...
}
```

## 11. 故障排除

### 11.1 常见问题

| 问题 | 可能原因 | 解决方案 |
|------|----------|----------|
| 文档中没有显示服务 | SwaggerResourceController 未正确配置 | 检查 SwaggerResourceController 中的服务列表 |
| 服务文档无法访问 | 服务未启动或路由配置错误 | 检查服务状态和网关路由配置 |
| 页面样式异常 | Knife4j 版本不兼容 | 确保使用兼容 Spring Boot 3.x 的版本 |
| 中文显示乱码 | 字符集配置问题 | 检查系统字符集和浏览器设置 |

### 11.2 日志排查

可以通过查看网关服务的日志来排查问题：

```bash
# 查看网关服务日志
./gradlew.bat :mom-gateway:bootRun --debug
```

## 12. 版本历史

| 版本 | 日期 | 描述 |
|------|------|------|
| v1.0 | 2026-01-14 | 初始版本，实现基础的 Knife4j 网关聚合 |

## 13. 相关链接

- [Knife4j 官方文档](https://doc.xiaominfo.com/knife4j/)
- [SpringDoc 官方文档](https://springdoc.org/)
- [OpenAPI 3.0 规范](https://swagger.io/specification/)

## 14. 维护说明

- 负责团队：LY-FactMesh 技术团队
- 联系邮箱：ly-factmesh@example.com
- 更新周期：每月更新一次文档
- 版本管理：遵循语义化版本规范

## 15. 手动部署说明

按照上述步骤配置完成后，可以直接使用以下命令启动服务：

1. **启动 admin 服务**：
   ```bash
   ./gradlew.bat :mom-admin:bootRun
   ```

2. **启动 gateway 服务**：
   ```bash
   ./gradlew.bat :mom-gateway:bootRun
   ```

3. **访问文档**：
   ```
   http://localhost:8080/doc.html
   ```

所有配置均已明确，按照文档操作后可直接运行查看效果。
