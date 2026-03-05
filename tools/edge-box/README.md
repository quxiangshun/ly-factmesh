# FactMesh 边缘盒子（Edge Box）

多语言边缘端数据采集与上报，均通过 MQTT 发布到 `factmesh/input/device/{deviceId}`，与 FactMesh Java 核心通信。

## 目录结构

```
edge-box/
├── c/        # C 语言 - 极端低资源（单片机、无 OS 嵌入式），paho.mqtt.c
├── go/       # Go 语言 - 资源适中（树莓派、工控机），paho.mqtt.golang
├── java/     # Java 语言 - 与核心同语言，fat jar，Paho MQTT Java
└── python/   # Python 语言 - 原型验证、脚本化，paho-mqtt
```

## 快速开始

| 语言 | 构建 | 运行 |
|------|------|------|
| C | `cd c && cmake -B build && cmake --build build` | `./build/factmesh_edge_box -b tcp://127.0.0.1:1883 -d sensor-001 -i 3` |
| Go | `cd go && go build -o factmesh_edge_box .` | `./factmesh_edge_box -b tcp://127.0.0.1:1883 -d sensor-001 -i 3` |
| Java | `./gradlew :tools:edge-box:java:jar` | `java -jar java/build/libs/factmesh-edge-box-java-1.0-SNAPSHOT.jar -b tcp://127.0.0.1:1883 -d sensor-001 -i 3` |
| Python | `pip install -r python/requirements.txt` | `python python/main.py -b 127.0.0.1 -d sensor-001 -i 3` |

## 通用参数

| 选项 | 说明 | 默认值 |
|------|------|--------|
| `-b` | MQTT Broker | tcp://127.0.0.1:1883 |
| `-d` | 设备 ID | edge-box-01 |
| `-i` | 发布周期（秒） | 5 |

## 前置条件

1. 启动 EMQX：`docker compose -f tools/mqtt/docker-compose.yml up -d`
2. FactMesh 配置 `infra.mqtt.broker-url` 并订阅 `factmesh/input/device/+`

---

## 边缘计算服务开发语言推荐（适配 FactMesh 场景）

按「适配优先级」排序，明确适用场景及与项目的兼容性。

### 1. Java（首选，无缝适配现有项目）

**核心优势**：FactMesh 基于 Java，选用 Java 开发边缘服务可直接复用 `MqttClientWrapper`、配置类，无需跨语言调用；生态完善，有 Spring Cloud Edge、Eclipse Kura 等；稳定性强，适合企业级边缘服务。

**适用场景**：FactMesh 边缘节点核心服务（数据接入、MQTT 消息处理、跨节点同步）、需与云端 Java 服务联动、边缘设备算力中等（工业网关、边缘服务器）。

**适配优化**：可用 GraalVM 编译为原生镜像，减少 JVM 占用；复用项目 Paho-MQTT 客户端。

### 2. Go（次选，轻量高性能）

**核心优势**：编译为二进制，无运行时依赖，资源占用低（内存约为 Java 的 1/5），启动快；原生并发（goroutine），适合高并发 MQTT 消息；有成熟的 paho.mqtt.golang。

**适用场景**：FactMesh 边缘轻量服务（MQTT 转发、设备采集）、算力有限设备（嵌入式、小型网关）、高并发场景。

**与项目适配**：作为 Java 核心的补充，开发轻量边缘代理，通过 MQTT 与 Java 核心通信，实现「核心服务（Java）+ 轻量代理（Go）」架构。

### 3. C/C++（极端低资源场景选用）

**核心优势**：执行效率高、内存极低、可直接操作硬件；有 paho.mqtt.c 等库，可精准适配边缘硬件。

**适用场景**：FactMesh 边缘硬件适配层（传感器采集、硬件控制）、无 OS 嵌入式设备、对资源和性能有极致要求。

**注意事项**：开发效率低、无 GC、需手动管理内存；与 Java 核心通过 MQTT/Socket 联动，仅在极端低资源场景选用。

### 4. Python（快速迭代、原型开发）

**核心优势**：语法简洁，开发效率高；有 paho-mqtt、gpiozero 等库；学习成本低，适合快速验证业务逻辑。

**适用场景**：边缘服务原型、短期迭代的轻量功能（数据可视化、简单告警）、算力充足且对实时性要求不高。

**注意事项**：解释型语言，执行效率较低；部署需 Python 运行时；仅推荐原型或非核心功能。

### 总结（适配 FactMesh 最优选择）

- **核心边缘服务**（与 MQTT 模块联动、跨节点同步）：优先 Java，无缝复用项目代码
- **轻量边缘代理、低算力设备**：选 Go，与 Java 核心通过 MQTT 联动
- **嵌入式硬件适配层**：选 C/C++，极致低资源占用
- **原型开发、快速验证**：选 Python，验证完成后可迁移为 Java/Go

---

详见各语言子目录 README。
