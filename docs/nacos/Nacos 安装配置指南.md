# Nacos 安装配置指南

## 1. 下载 Nacos Server

### 1.1 官方下载地址

访问 Nacos 官方 GitHub Release 页面下载最新稳定版本：

[https://github.com/alibaba/nacos/releases](https://github.com/alibaba/nacos/releases)

### 1.2 推荐版本

当前推荐使用 Nacos Server 2.3.2 版本，下载链接：

[https://github.com/alibaba/nacos/releases/download/2.3.2/nacos-server-2.3.2.zip](https://github.com/alibaba/nacos/releases/download/2.3.2/nacos-server-2.3.2.zip)

## 2. 安装 Nacos Server

### 2.1 解压安装包

将下载的 `nacos-server-2.3.2.zip` 解压到任意目录，例如：

```bash
# Windows 使用 PowerShell
Expand-Archive -Path nacos-server-2.3.2.zip -DestinationPath .

# Linux/Mac
unzip nacos-server-2.3.2.zip
```

解压后会得到一个 `nacos` 目录，其中包含 Nacos Server 的所有文件。

## 3. 启动 Nacos Server

### 3.1 单机模式启动

#### Windows

在 PowerShell 中执行：

```powershell
cd nacos\bin
.startup.cmd -m standalone
```

#### Linux/Mac

```bash
cd nacos/bin
sh startup.sh -m standalone
```

### 3.2 验证启动成功

启动成功后，可以通过以下方式验证：

1. 访问 Nacos 控制台：[http://localhost:8848/nacos](http://localhost:8848/nacos)
2. 默认用户名和密码：`nacos` / `nacos`
3. 查看启动日志：`nacos/logs/start.out`

### 3.3 Docker方式启动

#### 3.3.1 拉取Nacos Docker镜像

```bash
docker pull nacos/nacos-server:v2.3.2
```

#### 3.3.2 使用Docker命令启动（单机模式）

```bash
docker run -d \
  --name nacos \
  -p 8848:8848 \
  -p 9848:9848 \
  -p 9849:9849 \
  -e MODE=standalone \
  nacos/nacos-server:v2.3.2
```

#### 3.3.3 使用Docker Compose启动

创建 `docker-compose.yml` 文件：

```yaml
version: '3.8'

services:
  nacos:
    image: nacos/nacos-server:v2.3.2
    container_name: nacos
    environment:
      - MODE=standalone
    ports:
      - "8848:8848"
      - "9848:9848"
      - "9849:9849"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8848/nacos/v1/console/health/readiness"]
      interval: 30s
      timeout: 10s
      retries: 3
    restart: unless-stopped
```

然后执行：

```bash
docker compose up -d
```

#### 3.3.4 验证Docker方式启动成功

1. 访问 Nacos 控制台：[http://localhost:8848/nacos](http://localhost:8848/nacos)
2. 默认用户名和密码：`nacos` / `nacos`
3. 查看Docker容器状态：
   ```bash
   docker ps | grep nacos
   ```
4. 查看Docker日志：
   ```bash
   docker logs -f nacos
   ```

#### 3.3.5 Docker方式的高级配置

##### 3.3.5.1 使用外部数据库

Nacos默认使用嵌入式数据库Derby，生产环境建议使用外部数据库。以下是使用PostgreSQL的示例：

```yaml
version: '3.8'

services:
  nacos:
    image: nacos/nacos-server:v2.3.2
    container_name: nacos
    environment:
      - MODE=standalone
      - SPRING_DATASOURCE_PLATFORM=postgresql
      - POSTGRES_SERVICE_HOST=postgres
      - POSTGRES_SERVICE_PORT=5432
      - POSTGRES_SERVICE_DB_NAME=nacos
      - POSTGRES_SERVICE_USER=nacos
      - POSTGRES_SERVICE_PASSWORD=nacos
    ports:
      - "8848:8848"
      - "9848:9848"
      - "9849:9849"
    depends_on:
      postgres:
        condition: service_healthy
    restart: unless-stopped
  
  postgres:
    image: postgres:16.4
    container_name: postgres
    environment:
      POSTGRES_USER: nacos
      POSTGRES_PASSWORD: nacos
      POSTGRES_DB: nacos
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U nacos -d nacos"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

volumes:
  postgres_data:
    driver: local
```

##### 3.3.5.2 数据持久化

为了确保Nacos配置数据不丢失，可以使用Docker卷进行数据持久化：

```yaml
version: '3.8'

services:
  nacos:
    image: nacos/nacos-server:v2.3.2
    container_name: nacos
    environment:
      - MODE=standalone
      - SPRING_DATASOURCE_PLATFORM=derby
    ports:
      - "8848:8848"
      - "9848:9848"
      - "9849:9849"
    volumes:
      - nacos_data:/home/nacos/data
      - nacos_logs:/home/nacos/logs
    restart: unless-stopped

volumes:
  nacos_data:
    driver: local
  nacos_logs:
    driver: local
```

##### 3.3.5.3 自定义环境变量

可以通过环境变量自定义Nacos的各种配置：

| 环境变量 | 说明 | 默认值 |
|---------|-----|--------|
| MODE | 运行模式：standalone/cluster | standalone |
| NACOS_SERVER_PORT | Nacos服务端口 | 8848 |
| PREFER_HOST_MODE | 是否优先使用hostname | ip |
| NACOS_AUTH_ENABLE | 是否开启认证 | false |
| NACOS_AUTH_TOKEN_EXPIRE_SECONDS | 令牌过期时间 | 18000 |
| NACOS_AUTH_TOKEN | 令牌密钥 | 随机生成 |
| NACOS_AUTH_CACHE_ENABLE | 是否开启认证缓存 | false |

例如，开启认证并自定义令牌：

```bash
docker run -d \
  --name nacos \
  -p 8848:8848 \
  -p 9848:9848 \
  -p 9849:9849 \
  -e MODE=standalone \
  -e NACOS_AUTH_ENABLE=true \
  -e NACOS_AUTH_TOKEN=your-secret-token \
  nacos/nacos-server:v2.3.2
```

## 4. Nacos 配置

### 4.1 服务发现配置

在各微服务模块的 `application.yml` 文件中添加以下配置：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848  # Nacos 服务器地址
        namespace:                    # 命名空间，默认为空
        group: DEFAULT_GROUP          # 分组，默认为 DEFAULT_GROUP
        username: nacos               # 用户名
        password: nacos               # 密码
```

### 4.2 配置中心配置

在各微服务模块的 `application.yml` 文件中添加以下配置：

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848  # Nacos 服务器地址
        namespace:                    # 命名空间，默认为空
        group: DEFAULT_GROUP          # 分组，默认为 DEFAULT_GROUP
        username: nacos               # 用户名
        password: nacos               # 密码
        file-extension: yml           # 配置文件格式，支持 yml 和 properties
        import-check:
          enabled: false              # 禁用配置导入检查（可选）
```

## 5. 项目集成

### 5.1 依赖配置

在各微服务模块的 `build.gradle` 文件中添加以下依赖：

```gradle
dependencyManagement {
    imports {
        mavenBom org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES
        mavenBom 'org.springframework.cloud:spring-cloud-dependencies:2023.0.1'
        mavenBom 'com.alibaba.cloud:spring-cloud-alibaba-dependencies:2022.0.0.0-RC2'
    }
}

dependencies {
    // Nacos Discovery Starter
    implementation 'com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery'
    
    // Nacos Config Starter
    implementation 'com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config'
}
```

### 5.2 模块配置说明

#### mom-gateway 配置

```yaml
spring:
  application:
    name: mom-gateway
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        username: nacos
        password: nacos
      config:
        server-addr: localhost:8848
        username: nacos
        password: nacos
        file-extension: yml
        import-check:
          enabled: false
    gateway:
      routes:
        - id: mom-admin
          uri: lb://mom-admin  # 使用服务发现路由到 admin 服务
          predicates:
            - Path=/admin/**
          filters:
            - StripPrefix=1
```

#### mom-admin 配置

```yaml
spring:
  application:
    name: mom-admin
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        username: nacos
        password: nacos
      config:
        server-addr: localhost:8848
        username: nacos
        password: nacos
        file-extension: yml
        import-check:
          enabled: false
```

## 6. Nacos 控制台使用

### 6.1 服务管理

1. 登录 Nacos 控制台
2. 点击左侧导航栏的「服务管理」→「服务列表」
3. 可以查看、编辑和删除已注册的服务

### 6.2 配置管理

1. 登录 Nacos 控制台
2. 点击左侧导航栏的「配置管理」→「配置列表」
3. 可以添加、编辑、删除和发布配置

### 6.3 命名空间管理

1. 登录 Nacos 控制台
2. 点击左侧导航栏的「命名空间」
3. 可以添加、编辑和删除命名空间

## 7. 停止 Nacos Server

### 7.1 传统方式停止

#### 7.1.1 Windows

在 PowerShell 中执行：

```powershell
cd nacos\bin
.shutdown.cmd
```

#### 7.1.2 Linux/Mac

```bash
cd nacos/bin
sh shutdown.sh
```

### 7.2 Docker方式停止

#### 7.2.1 使用Docker命令停止

```bash
docker stop nacos
```

#### 7.2.2 使用Docker Compose停止

```bash
docker compose down
```

## 8. 常见问题

### 8.1 端口被占用

如果 8848 端口被占用，可以修改 Nacos 配置文件 `nacos/conf/application.properties` 中的端口配置：

```properties
server.port=8849
```

### 8.2 启动失败

检查启动日志 `nacos/logs/start.out` 中的错误信息，常见原因：

1. JDK 版本不兼容（推荐使用 JDK 8+）
2. 端口被占用
3. 配置文件错误

### 8.3 服务注册失败

1. 检查 Nacos 服务器是否正常运行
2. 检查服务配置中的 `server-addr` 是否正确
3. 检查用户名和密码是否正确
4. 检查网络连接是否正常

## 9. 相关链接

- [Nacos 官方文档](https://nacos.io/zh-cn/docs/quick-start.html)
- [Nacos GitHub](https://github.com/alibaba/nacos)
- [Spring Cloud Alibaba Nacos 文档](https://sca.aliyun.com/docs/2023/user-guide/nacos/)
