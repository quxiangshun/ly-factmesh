# Docker配置与使用说明

## 1. 已创建的Dockerfile文件

本项目为每个服务模块创建了独立的Dockerfile，采用分层构建方式，确保镜像轻量且高效。

| 模块名称 | Dockerfile路径 | 说明 |
|---------|--------------|-----|
| mom-iot | mom-iot/Dockerfile | 物联网设备管理模块 |
| mom-admin | mom-admin/Dockerfile | 系统管理模块 |
| mom-mes | mom-mes/Dockerfile | 制造执行系统模块 |
| mom-wms | mom-wms/Dockerfile | 仓库管理系统模块 |
| mom-qms | mom-qms/Dockerfile | 质量管理系统模块 |
| mom-gateway | mom-gateway/Dockerfile | API网关模块 |

## 2. Dockerfile特点

1. **分层构建**
   - 采用Spring Boot官方推荐的分层构建方式
   - 分为构建阶段和运行阶段，减小最终镜像体积
   - 构建阶段使用`openjdk:25-slim`，包含编译工具
   - 运行阶段使用`openjdk:25-slim`，仅包含运行环境

2. **轻量级镜像**
   - 基于`openjdk:25-slim`基础镜像，体积小
   - 仅包含运行应用所需的最小依赖
   - 减少镜像拉取和部署时间

3. **健康检查**
   - 内置健康检查配置，确保服务正常运行
   - 可配置检查间隔、超时时间和重试次数
   - 支持通过环境变量调整检查参数

4. **环境变量支持**
   - 支持通过环境变量配置服务
   - 默认使用生产环境配置
   - 可灵活调整数据源、服务注册中心等配置

5. **标准化配置**
   - 所有模块使用统一的Dockerfile模板
   - 一致的构建和运行配置
   - 易于维护和扩展

## 3. docker-compose.yml功能

`docker-compose.yml`文件定义了完整的服务栈，包含以下功能：

1. **完整服务生态**
   - PostgreSQL数据库服务
   - Nacos服务注册中心
   - 所有业务模块服务

2. **依赖管理**
   - 配置服务间的依赖关系
   - 确保服务按正确顺序启动
   - 支持健康检查依赖条件

3. **端口映射**
   - 为每个服务配置独立的端口映射
   - 方便外部访问和调试
   - 避免端口冲突

4. **环境配置**
   - 集中管理所有服务的环境变量
   - 配置数据源连接信息
   - 配置服务注册中心地址

5. **数据持久化**
   - 配置PostgreSQL数据持久化
   - 使用Docker卷保存数据
   - 确保数据不丢失

6. **健康检查**
   - 为每个服务配置健康检查
   - 自动重启失败的服务
   - 确保服务高可用性

## 4. 使用说明

### 4.1 构建镜像

**构建单个服务镜像**：
```bash
docker build -t mom-iot:1.0 -f mom-iot/Dockerfile .
```

**构建所有服务镜像**：
```bash
docker-compose build
```

### 4.2 启动服务

**启动所有服务**：
```bash
docker-compose up -d
```

**启动特定服务**：
```bash
docker-compose up -d mom-iot mom-admin
```

### 4.3 查看服务状态

**查看所有服务状态**：
```bash
docker-compose ps
```

**查看特定服务日志**：
```bash
docker-compose logs -f mom-iot
```

**查看所有服务日志**：
```bash
docker-compose logs -f
```

### 4.4 停止服务

**停止所有服务**：
```bash
docker-compose down
```

**停止特定服务**：
```bash
docker-compose stop mom-iot mom-admin
```

### 4.5 重启服务

**重启所有服务**：
```bash
docker-compose restart
```

**重启特定服务**：
```bash
docker-compose restart mom-iot
```

### 4.6 查看服务健康状态

**查看服务健康状态**：
```bash
docker-compose ps --services --filter "health=unhealthy"
```

## 5. 服务访问地址

启动成功后，可以通过以下地址访问各服务：

| 服务名称 | 访问地址 | 说明 |
|---------|---------|-----|
| PostgreSQL | localhost:5432 | 数据库服务（postgres/postgres） |
| MySQL (Nacos) | localhost:3306 | Nacos 元数据存储 |
| Nacos | http://localhost:8848/nacos | 服务注册中心 |
| mom-gateway | http://localhost:8080 | API 网关（统一入口） |
| mom-admin | http://localhost:8081 | 系统管理 |
| mom-iot | http://localhost:8082 | 物联网设备管理 |
| mom-mes | http://localhost:8083 | 制造执行系统 |
| mom-wms | http://localhost:8084 | 仓库管理系统 |
| mom-qms | http://localhost:8085 | 质量管理系统 |

## 6. 环境变量配置

可以通过修改`docker-compose.yml`中的环境变量来调整服务配置：

| 环境变量 | 说明 | 示例值 |
|---------|-----|-------|
| SPRING_PROFILES_ACTIVE | 运行环境 | prod, dev, test |
| SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR | 服务注册中心地址 | nacos:8848 |
| SPRING_DATASOURCE_URL | 数据库连接URL | jdbc:postgresql://postgres:5432/ly_factmesh_admin |
| SPRING_DATASOURCE_USERNAME | 数据库用户名 | postgres |
| SPRING_DATASOURCE_PASSWORD | 数据库密码 | postgres |

## 7. 常见问题处理

### 7.1 端口冲突

如果遇到端口冲突，可以修改`docker-compose.yml`中的端口映射：

```yaml
mom-iot:
  ports:
    - "8082:8080"  # 修改左边的主机端口
```

### 7.2 服务启动失败

查看服务日志找出失败原因：

```bash
docker-compose logs -f <服务名称>
```

### 7.3 数据库连接失败

确保PostgreSQL服务正常运行，检查环境变量配置：

```bash
docker-compose ps postgres  # 检查PostgreSQL状态
docker-compose logs -f postgres  # 查看PostgreSQL日志
```

### 7.4 服务注册失败

确保Nacos服务正常运行，检查环境变量配置：

```bash
docker-compose ps nacos  # 检查Nacos状态
docker-compose logs -f nacos  # 查看Nacos日志
```

## 8. 最佳实践

1. **定期更新基础镜像**
   - 定期更新`openjdk:25-slim`基础镜像
   - 确保使用最新的安全补丁

2. **使用固定版本标签**
   - 避免使用`latest`标签
   - 使用固定版本标签，如`openjdk:25-slim`

3. **最小化镜像层数**
   - 合并多个RUN命令
   - 清理构建过程中的临时文件

4. **使用.dockerignore文件**
   - 排除不必要的文件和目录
   - 减少构建上下文大小
   - 加速构建过程

5. **监控服务状态**
   - 定期检查服务健康状态
   - 设置适当的健康检查参数
   - 配置自动重启策略

## 9. 扩展建议

1. **添加CI/CD集成**
   - 集成Jenkins或GitHub Actions
   - 自动构建和部署镜像

2. **使用Docker Swarm或Kubernetes**
   - 对于生产环境，考虑使用容器编排工具
   - 提供更高的可用性和可扩展性

3. **添加日志管理**
   - 集成ELK或Loki
   - 集中管理和分析日志

4. **添加监控系统**
   - 集成Prometheus和Grafana
   - 监控服务性能和资源使用情况

5. **使用私有镜像仓库**
   - 搭建私有Docker Registry
   - 安全管理镜像

通过以上配置和使用说明，您可以轻松构建、部署和管理本项目的Docker容器服务。