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

详见各语言子目录 README。
